package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkophosReaver.class, Unhinge.class})
class SkophosReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Skophos Reaver gets +2/+0 during its controller's turn")
    void getsBoostDuringControllersTurnOnly() {
        Permanent reaver = addCreatureReady(player1, new SkophosReaver());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, reaver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, reaver)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, reaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, reaver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Discarding Skophos Reaver offers its madness cost")
    void discardTriggersMadness() {
        SkophosReaver reaver = discardViaUnhinge();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(reaver.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting madness casts Skophos Reaver for {1}{R}")
    void acceptingMadnessCastsCreature() {
        SkophosReaver reaver = discardViaUnhinge();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(reaver.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining madness puts Skophos Reaver into its owner's graveyard")
    void decliningMadnessGoesToGraveyard() {
        SkophosReaver reaver = discardViaUnhinge();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(reaver.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(reaver.getId()));
    }

    private SkophosReaver discardViaUnhinge() {
        SkophosReaver reaver = new SkophosReaver();
        harness.setHand(player1, List.of(reaver));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return reaver;
    }
}
