package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FalkenrathReaver;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoldarenPariah.class, FalkenrathReaver.class, RavensCrime.class, ZulaportCutthroat.class})
class VoldarenPariahTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing three other creatures transforms Voldaren Pariah")
    void sacrificesThreeOtherCreaturesAndTransforms() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());
        Permanent third = addCreatureReady(player1, new FalkenrathReaver());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.getCard().getName()).isEqualTo("Abolisher of Bloodlines");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard(), third.getCard());
    }

    @Test
    @DisplayName("Abolisher of Bloodlines makes the targeted opponent sacrifice three creatures")
    void targetedOpponentSacrificesThreeCreatures() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player1, new FalkenrathReaver());
        Permanent opponentFirst = addCreatureReady(player2, new FalkenrathReaver());
        Permanent opponentSecond = addCreatureReady(player2, new FalkenrathReaver());
        Permanent opponentThird = addCreatureReady(player2, new FalkenrathReaver());
        Permanent opponentRemaining = addCreatureReady(player2, new FalkenrathReaver());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(opponentFirst.getId(), opponentSecond.getId(), opponentThird.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentRemaining);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentFirst.getCard(), opponentSecond.getCard(), opponentThird.getCard());
    }

    @Test
    @DisplayName("The transform trigger cannot target its controller")
    void transformTriggerCannotTargetController() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player1, new FalkenrathReaver());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Madness offers Voldaren Pariah for {B}{B}{B}")
    void madnessOffersCast() {
        VoldarenPariah pariah = discardPariahViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(pariah.getId()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void decliningMadnessPutsPariahInGraveyard() {
        VoldarenPariah pariah = discardPariahViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(pariah);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pariah);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void madnessCastsPariahForThreeBlackManaDuringOpponentsTurn() {
        VoldarenPariah pariah = discardPariahViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(pariah.getId())
                        && !permanent.isTransformed());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(pariah);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pariah);
    }

    @Test
    void castingFrontFaceDoesNotTriggerOpponentSacrifice() {
        Permanent opponentCreature = addCreatureReady(player2, new FalkenrathReaver());
        harness.castFromHand(player1, new VoldarenPariah(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Voldaren Pariah").isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateWithOnlyTwoOtherCreatures() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, pariah), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(pariah, first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(pariah.isTransformed()).isFalse();
    }

    @Test
    void tappedSummoningSickPariahPaysSacrificeCostBeforeTransformResolves() {
        Permanent pariah = harness.addToBattlefieldAndReturn(player1, new VoldarenPariah());
        pariah.setSummoningSick(true);
        pariah.tap();
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());
        Permanent third = addCreatureReady(player1, new FalkenrathReaver());
        Permanent opponentCreature = addCreatureReady(player2, new FalkenrathReaver());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(pariah);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard(), third.getCard());
        assertThat(pariah.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentCreature);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(pariah.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature.getCard());
    }

    @Test
    void cannotSacrificePariahToItsOwnActivation() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player1, new FalkenrathReaver());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, pariah.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pariah);
        assertThat(pariah.isTransformed()).isFalse();
    }

    @Test
    void opponentWithTwoCreaturesSacrificesBoth() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player1, new FalkenrathReaver());
        Permanent opponentFirst = addCreatureReady(player2, new FalkenrathReaver());
        Permanent opponentSecond = addCreatureReady(player2, new FalkenrathReaver());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentFirst.getCard(), opponentSecond.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentCreaturesDieSimultaneouslyWhenAllThreeMustBeSacrificed() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player2, new ZulaportCutthroat());
        addCreatureReady(player2, new FalkenrathReaver());
        addCreatureReady(player2, new FalkenrathReaver());
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife + 3);
    }

    @Test
    void multipleActivationsOnlyTransformOnce() {
        Permanent pariah = addCreatureReady(player1, new VoldarenPariah());
        Permanent first = addCreatureReady(player1, new FalkenrathReaver());
        Permanent second = addCreatureReady(player1, new FalkenrathReaver());
        Permanent third = addCreatureReady(player1, new FalkenrathReaver());
        Permanent fourth = addCreatureReady(player1, new FalkenrathReaver());
        Permanent fifth = addCreatureReady(player1, new FalkenrathReaver());
        addCreatureReady(player1, new FalkenrathReaver());

        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.activateAbility(player1, indexOf(player1, pariah), null, null);
        harness.handlePermanentChosen(player1, fourth.getId());
        harness.handlePermanentChosen(player1, fifth.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(pariah.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(pariah);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private VoldarenPariah discardPariahViaRavensCrime() {
        VoldarenPariah pariah = new VoldarenPariah();
        harness.setHand(player1, List.of(pariah));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return pariah;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
