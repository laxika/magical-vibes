package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrbanRetreat.class, GrizzlyBears.class})
class UrbanRetreatTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and produces one of three colors")
    void entersTappedAndProducesChosenMana() {
        UrbanRetreat retreatCard = new UrbanRetreat();
        harness.setHand(player1, List.of(retreatCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent retreat = findPermanent(player1, "Urban Retreat");
        assertThat(retreat.isTapped()).isTrue();

        retreat.untap();
        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE", "BLUE");

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(retreat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns a tapped creature as a cost and puts itself onto the battlefield tapped")
    void returnsTappedCreatureAndReturnsFromHand() {
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent secondTappedCreature = addCreatureReady(player1, new GrizzlyBears());
        secondTappedCreature.tap();
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        UrbanRetreat retreatCard = new UrbanRetreat();
        harness.setHand(player1, List.of(retreatCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbility(player1, 0, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(tappedCreature.getId(), secondTappedCreature.getId());
        assertThat(choice.validIds()).doesNotContain(untappedCreature.getId());

        harness.handlePermanentChosen(player1, tappedCreature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(retreatCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(untappedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondTappedCreature);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(retreatCard));

        Permanent enteredRetreat = findPermanent(player1, "Urban Retreat");
        assertThat(enteredRetreat.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(tappedCreature.getCard());
    }
}
