package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Counterintelligence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.w.WuEliteCavalry;
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

@CardUsed({RidingTheDiluHorse.class, ForestBear.class, Forest.class,
        Counterintelligence.class, WuEliteCavalry.class, Humble.class})
class RidingTheDiluHorseTest extends BaseCardTest {

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new ForestBear());
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new RidingTheDiluHorse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Resolving gives +2/+2 and horsemanship to the target creature")
    void resolvesBoostAndHorsemanship() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void targetsOpponentCreature() {
        Permanent target = addCreatureReady(player2, new ForestBear());
        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Boost and horsemanship last indefinitely (survive end of turn)")
    void lastsIndefinitely() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Two copies on the same creature stack additively")
    void copiesStack() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Does nothing if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addReadyCreature();
        castOn(target);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Riding the Dilu Horse");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ForestBear()); // legal creature target so the spell is castable (CR 601.2c)
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> castOn(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Granted horsemanship prevents ordinary blockers but permits horsemanship blockers")
    void grantedHorsemanshipRestrictsBlocking() {
        Permanent attacker = addReadyCreature();
        Permanent ordinaryBlocker = addCreatureReady(player2, new ForestBear());
        Permanent horseman = addCreatureReady(player2, new WuEliteCavalry());
        castOn(attacker);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, ordinaryBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, horseman, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Returning and recasting the creature does not retain the indefinite effects")
    void effectsDoNotFollowCreatureThroughHand() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Counterintelligence()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.assertInHand(player1, "Forest Bear");
        harness.assertNotOnBattlefield(player1, "Forest Bear");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Forest Bear");

        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HORSEMANSHIP)).isFalse();
    }

    @Test
    @DisplayName("A later ability-removal effect removes horsemanship but preserves the power bonus")
    void laterAbilityRemovalSuppressesHorsemanshipUntilCleanup() {
        Permanent target = addReadyCreature();
        castOn(target);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    @DisplayName("Horsemanship granted after ability removal survives that earlier removal")
    void laterGrantSurvivesEarlierAbilityRemoval() {
        Permanent target = addReadyCreature();
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        castOn(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HORSEMANSHIP)).isTrue();
    }
}
