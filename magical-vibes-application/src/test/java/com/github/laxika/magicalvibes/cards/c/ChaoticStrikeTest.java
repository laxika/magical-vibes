package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaoticStrike.class, RagingKavu.class, Mountain.class})
class ChaoticStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("A won flip grants +1/+1 and a lost flip leaves the creature unchanged")
    void coinFlipDecidesBoost() {
        Permanent target = castAgainstCreature();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip"));

        if (won) {
            assertThat(target.getEffectivePower()).isEqualTo(4);
            assertThat(target.getEffectiveToughness()).isEqualTo(2);
        } else {
            assertThat(target.getEffectivePower()).isEqualTo(3);
            assertThat(target.getEffectiveToughness()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("The spell draws a card immediately regardless of the flip")
    void drawsCard() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new RagingKavu());
        harness.setHand(player1, List.of(new ChaoticStrike()));
        harness.setLibrary(player1, List.of(new RagingKavu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chaotic Strike");
        harness.assertInHand(player1, "Raging Kavu");
    }

    @Test
    @DisplayName("The temporary boost wears off at end of turn")
    void boostWearsOff() {
        Permanent target = castAgainstCreature();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot be cast before blockers are declared")
    void cannotCastBeforeBlockers() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new RagingKavu());
        harness.setHand(player1, List.of(new ChaoticStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        addCreatureReady(player2, new RagingKavu());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new ChaoticStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be cast after blockers are declared")
    void canCastAfterBlockersAreDeclared() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new RagingKavu());
        addCreatureReady(player2, new RagingKavu());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.setHand(player1, List.of(new ChaoticStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chaotic Strike");
    }

    @Test
    @DisplayName("Can be cast during the combat damage step")
    void canCastDuringCombatDamage() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new RagingKavu());
        harness.setHand(player1, List.of(new ChaoticStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chaotic Strike");
    }

    @Test
    @DisplayName("Cannot be cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new RagingKavu());
        harness.setHand(player1, List.of(new ChaoticStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent castAgainstCreature() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new RagingKavu());
        addCreatureReady(player1, new RagingKavu());
        harness.setHand(player1, List.of(new ChaoticStrike()));
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        return target;
    }
}
