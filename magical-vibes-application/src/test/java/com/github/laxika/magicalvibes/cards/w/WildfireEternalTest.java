package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WildfireEternalTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = new Permanent(new WildfireEternal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(atk);
        return atk;
    }

    private void addBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
    }

    @Test
    @DisplayName("Unblocked: may cast an instant/sorcery from hand for free")
    void unblockedMayCastSpellForFree() {
        Divination spell = new Divination();
        harness.setHand(player1, new ArrayList<>(List.of(spell)));
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Unblocked: creature in hand is not offered")
    void unblockedDoesNotOfferCreature() {
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves the spell in hand")
    void decliningCastsNothing() {
        Divination spell = new Divination();
        harness.setHand(player1, new ArrayList<>(List.of(spell)));
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Afflict 4: becoming blocked makes the defending player lose 4 life")
    void blockedAfflictsDefender() {
        harness.setHand(player1, new ArrayList<>(List.of(new Divination())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker();
        addBlocker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Afflict fires; free-cast does not (creature was blocked).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
