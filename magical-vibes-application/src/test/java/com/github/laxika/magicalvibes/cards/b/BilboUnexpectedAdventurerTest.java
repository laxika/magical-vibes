package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BilboUnexpectedAdventurer.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class BilboUnexpectedAdventurerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage offers one nonland permanent card with mana value 3 or less")
    void combatDamageReturnsEligibleCardUnderItsOwnersControl() {
        Card legal = new GrizzlyBears();
        legal.setOwnerId(player2.getId());
        Card tooExpensive = new HillGiant();
        tooExpensive.setOwnerId(player2.getId());
        Card land = new Forest();
        land.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(legal, tooExpensive, land));

        addBilboAttacking();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());

        harness.handleMultipleCardsChosen(player1, List.of(legal.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
    }

    @Test
    @DisplayName("The up-to-one graveyard return may be declined")
    void combatDamageMayReturnNothing() {
        Card legal = new GrizzlyBears();
        legal.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(legal));

        addBilboAttacking();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(legal);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
    }

    @Test
    @DisplayName("Bilbo cannot be blocked by a creature with power 3 or greater")
    void cannotBeBlockedByPowerThreeCreature() {
        Permanent bilbo = addCreatureReady(player1, new BilboUnexpectedAdventurer());
        bilbo.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bilbo);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    private void addBilboAttacking() {
        Permanent bilbo = addCreatureReady(player1, new BilboUnexpectedAdventurer());
        bilbo.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
