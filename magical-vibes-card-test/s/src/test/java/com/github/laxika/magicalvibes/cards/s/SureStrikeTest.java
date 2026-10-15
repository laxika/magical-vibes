package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.PillarOfOrigins;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SureStrike.class, QueensBaySoldier.class, PillarOfOrigins.class})
class SureStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sure Strike gives +3/+0 and first strike")
    void resolvesAndBoostsTarget() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Boost and first strike wear off at cleanup step")
    void effectsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Sure Strike fizzles if target is removed")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        harness.castInstant(player1, 0, bearId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot cast Sure Strike without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new SureStrike()));

        UUID bearId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addToBattlefield(player1, new PillarOfOrigins());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Pillar of Origins");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = harness.getPermanentId(player2, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Repeated casts stack the power boost without granting double strike")
    void repeatedCastsStackPowerBoost() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new SureStrike(), new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = findPermanent(player1, "Queen's Bay Soldier");
        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike kills a blocker before it can deal damage")
    void grantedFirstStrikeWorksInCombat() {
        Permanent attacker = addCreatureReady(player1, new QueensBaySoldier());
        addCreatureReady(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Queen's Bay Soldier");
        harness.assertInGraveyard(player2, "Queen's Bay Soldier");
        harness.assertLife(player2, 20);
        assertThat(attacker.getMarkedDamage()).isZero();
    }
}
