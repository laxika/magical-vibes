package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerrorOfTowashi.class, GrizzlyBears.class, Shock.class})
class TerrorOfTowashiTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger chooses no graveyard target before payment")
    void choosesTargetAfterPayment() {
        addCreatureReady(player1, new TerrorOfTowashi());
        Card firstCreature = new GrizzlyBears();
        Card nonCreature = new Shock();
        Card secondCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, nonCreature, secondCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Paying {3}{B} returns an own graveyard creature as a Phyrexian")
    void payingReturnsCreatureAsPhyrexian() {
        addCreatureReady(player1, new TerrorOfTowashi());
        Card firstCreature = new GrizzlyBears();
        Card nonCreature = new Shock();
        Card returnedCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, nonCreature, returnedCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 2);

        harness.handleGraveyardCardChosen(player1, 2);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedCreature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.PHYREXIAN);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(returnedCreature);
    }

    @Test
    @DisplayName("Declining the payment leaves the graveyard unchanged")
    void decliningPaymentDoesNotReturnCreature() {
        addCreatureReady(player1, new TerrorOfTowashi());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }
}
