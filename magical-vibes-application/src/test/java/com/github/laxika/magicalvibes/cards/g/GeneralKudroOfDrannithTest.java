package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
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

@CardUsed({GeneralKudroOfDrannith.class, EliteVanguard.class, GrizzlyBears.class, ColossalDreadmaw.class})
class GeneralKudroOfDrannithTest extends BaseCardTest {

    @Test
    void boostsOtherHumansYouControl() {
        addCreatureReady(player1, new GeneralKudroOfDrannith());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent nonHuman = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.computeStaticBonus(gd, human).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, human).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, nonHuman).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, nonHuman).toughness()).isZero();
    }

    @Test
    void triggersWhenAnotherHumanEntersAndOnlyTargetsOpponentsGraveyard() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        addCreatureReady(player1, new GeneralKudroOfDrannith());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCard.getId());
    }

    @Test
    void triggersWhenGeneralKudroEnters() {
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new GeneralKudroOfDrannith()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
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
        Permanent firstHuman = addCreatureReady(player1, new EliteVanguard());
        Permanent secondHuman = addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new ColossalDreadmaw());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(firstHuman.getId(), secondHuman.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(general).doesNotContain(firstHuman, secondHuman);
        harness.assertInGraveyard(player1, "Elite Vanguard");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    void cannotTargetCreatureWithPowerLessThanFour() {
        addCreatureReady(player1, new GeneralKudroOfDrannith());
        addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
