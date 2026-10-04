package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HoneyMammoth;
import com.github.laxika.magicalvibes.cards.c.CheckpointOfficer;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.d.DaysquadMarshal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneralKudroOfDrannith.class, CheckpointOfficer.class, AlmightyBrushwagg.class,
        HoneyMammoth.class, DaysquadMarshal.class})
class GeneralKudroOfDrannithTest extends BaseCardTest {

    @Test
    void boostsOtherHumansYouControl() {
        addCreatureReady(player1, new GeneralKudroOfDrannith());
        Permanent human = addCreatureReady(player1, new CheckpointOfficer());
        Permanent nonHuman = addCreatureReady(player1, new AlmightyBrushwagg());

        assertThat(gqs.computeStaticBonus(gd, human).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, human).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, nonHuman).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, nonHuman).toughness()).isZero();
    }

    @Test
    void triggersWhenAnotherHumanEntersAndOnlyTargetsOpponentsGraveyard() {
        Card ownCard = new AlmightyBrushwagg();
        Card opponentCard = new AlmightyBrushwagg();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        addCreatureReady(player1, new GeneralKudroOfDrannith());

        harness.castFromHand(player1, new CheckpointOfficer(), "{1}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Almighty Brushwagg");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCard.getId());
    }

    @Test
    void triggersWhenGeneralKudroEnters() {
        Card opponentCard = new AlmightyBrushwagg();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.castFromHand(player1, new GeneralKudroOfDrannith(), "{1}{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCard.getId());
    }

    @Test
    void sacrificesTwoHumansToDestroyLargeCreature() {
        Permanent general = addCreatureReady(player1, new GeneralKudroOfDrannith());
        Permanent firstHuman = addCreatureReady(player1, new CheckpointOfficer());
        Permanent secondHuman = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player2, new HoneyMammoth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.handlePermanentChosen(player1, firstHuman.getId());
        harness.handlePermanentChosen(player1, secondHuman.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(general).doesNotContain(firstHuman, secondHuman);
        harness.assertInGraveyard(player1, "Checkpoint Officer");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Honey Mammoth");
    }

    @Test
    void cannotTargetCreatureWithPowerLessThanFour() {
        addCreatureReady(player1, new GeneralKudroOfDrannith());
        addCreatureReady(player1, new CheckpointOfficer());
        addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player2, new AlmightyBrushwagg());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotBoostItselfOrOpponentsHumans() {
        Permanent general = addCreatureReady(player1, new GeneralKudroOfDrannith());
        Permanent opponentHuman = addCreatureReady(player2, new CheckpointOfficer());

        assertThat(gqs.computeStaticBonus(gd, general).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, general).toughness()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opponentHuman).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opponentHuman).toughness()).isZero();
    }

    @Test
    void doesNotTriggerWhenNonHumanEnters() {
        Card opponentCard = new AlmightyBrushwagg();
        harness.setGraveyard(player2, List.of(opponentCard));
        addCreatureReady(player1, new GeneralKudroOfDrannith());

        harness.castFromHand(player1, new AlmightyBrushwagg(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenOpponentsHumanEnters() {
        Card opponentCard = new AlmightyBrushwagg();
        harness.setGraveyard(player2, List.of(opponentCard));
        addCreatureReady(player1, new GeneralKudroOfDrannith());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new CheckpointOfficer(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void canSacrificeGeneralItselfAndAbilityStillResolves() {
        Permanent general = addCreatureReady(player1, new GeneralKudroOfDrannith());
        Permanent human = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player2, new HoneyMammoth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.handlePermanentChosen(player1, general.getId());
        harness.handlePermanentChosen(player1, human.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(general, human);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "General Kudro of Drannith");
        harness.assertInGraveyard(player1, "Checkpoint Officer");
        harness.assertInGraveyard(player2, "Honey Mammoth");
    }

    @Test
    void cannotPayWithOnlyOneHumanAndANonHuman() {
        addCreatureReady(player1, new GeneralKudroOfDrannith());
        addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent target = addCreatureReady(player2, new HoneyMammoth());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "General Kudro of Drannith");
        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDestroyTargetWhosePowerDropsBelowFourWhenKudroIsSacrificed() {
        Permanent general = addCreatureReady(player1, new GeneralKudroOfDrannith());
        Permanent human = addCreatureReady(player1, new CheckpointOfficer());
        Permanent target = addCreatureReady(player1, new DaysquadMarshal());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.handlePermanentChosen(player1, general.getId());
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "General Kudro of Drannith");
        harness.assertInGraveyard(player1, "Checkpoint Officer");
        harness.assertNotInGraveyard(player1, "Daysquad Marshal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithNoOpponentGraveyardTargetsDoesNotAskForAChoice() {
        Card ownCard = new AlmightyBrushwagg();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of());

        harness.castFromHand(player1, new GeneralKudroOfDrannith(), "{1}{W}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "General Kudro of Drannith");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
