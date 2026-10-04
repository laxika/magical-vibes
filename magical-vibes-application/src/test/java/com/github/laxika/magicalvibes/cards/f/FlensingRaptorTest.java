package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.s.SinewDancer;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlensingRaptor.class, CrawlingChorus.class, SinewDancer.class})
class FlensingRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another toxic creature +1/+1 and flying until end of turn")
    void etbBoostsAndGrantsFlyingToToxicCreature() {
        Permanent toxicCreature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        castAndResolve(toxicCreature);

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(2);
        assertThat(toxicCreature.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The target must be a toxic creature you control")
    void cannotTargetNonToxicCreature() {
        Permanent nonToxicCreature = harness.addToBattlefieldAndReturn(player1, new SinewDancer());
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, nonToxicCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control with toxic");
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostAndFlyingWearOffAtEndOfTurn() {
        Permanent toxicCreature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        castAndResolve(toxicCreature);

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);
        assertThat(toxicCreature.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(1);
        assertThat(toxicCreature.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("ETB fizzles if the toxic target leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        Permanent toxicCreature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, toxicCreature.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(toxicCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target an opponent's toxic creature")
    void cannotTargetOpponentsToxicCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enter with no other toxic creatures without targeting itself")
    void entersWithoutLegalTargets() {
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flensing Raptor");
        assertThat(gd.stack).isEmpty();
        Permanent raptor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another Flensing Raptor is a legal target")
    void canTargetAnotherRaptor() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlensingRaptor());
        castAndResolve(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB does not affect a target that changes controllers before resolution")
    void targetChangingControllersMakesTriggerFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("ETB still resolves after Flensing Raptor leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> !permanent.getId().equals(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flensing Raptor deals combat damage and gives one poison counter without a trigger")
    void toxicAppliesAlongsideCombatDamage() {
        Permanent raptor = addCreatureReady(player1, new FlensingRaptor());
        raptor.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new FlensingRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();
    }
}
