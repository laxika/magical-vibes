package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FearOfLostTeeth;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({KonaRescueBeastie.class, FearOfLostTeeth.class, Forest.class, Murder.class})
class KonaRescueBeastieTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Kona may put a permanent card from hand onto the battlefield")
    void tappedKonaPutsPermanentFromHandOntoBattlefield() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card creature = new FearOfLostTeeth();
        Card land = new Forest();
        Card instant = new Murder();
        harness.setHand(player1, List.of(creature, land, instant));
        kona.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, instant);
    }

    @Test
    @DisplayName("Kona's controller may decline its Survival ability")
    void mayDeclinePuttingPermanent() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card creature = new FearOfLostTeeth();
        harness.setHand(player1, List.of(creature));
        kona.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    @DisplayName("An untapped Kona does not trigger Survival")
    void untappedKonaDoesNotTrigger() {
        harness.addToBattlefield(player1, new KonaRescueBeastie());
        Card creature = new FearOfLostTeeth();
        harness.setHand(player1, List.of(creature));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Untapping Kona before its Survival ability resolves prevents the effect")
    void untappingBeforeResolutionPreventsEffect() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card creature = new FearOfLostTeeth();
        harness.setHand(player1, List.of(creature));
        kona.tap();

        advanceToPostcombatMain();
        kona.untap();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    void putsLandOntoBattlefieldWithoutUsingLandPlay() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card land = new Forest();
        harness.setHand(player1, List.of(land));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        kona.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && !permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPutInstantOntoBattlefield() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card instant = new Murder();
        harness.setHand(player1, List.of(instant));
        kona.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringThirdMainPhase() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        harness.setHand(player1, List.of(new Forest()));
        kona.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        gd.additionalCombatMainPhasePairs = 1;
        harness.passUntilWithNoAttackers(player1, TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void opponentKonaDoesNotTriggerDuringYourSecondMainPhase() {
        Permanent kona = harness.addToBattlefieldAndReturn(player2, new KonaRescueBeastie());
        harness.setHand(player2, List.of(new Forest()));
        kona.tap();

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void tappedKonaLeavingBattlefieldStillAllowsPuttingPermanent() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card land = new Forest();
        harness.setHand(player1, List.of(land));
        harness.setHand(player2, List.of(new Murder()));
        kona.tap();

        advanceToPostcombatMain();
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, kona.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kona);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
    }
}
