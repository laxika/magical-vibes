package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({TimelessWitness.class, Shock.class, GrizzlyBears.class})
class TimelessWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a chosen card from your graveyard to your hand")
    void etbReturnsCardFromGraveyardToHand() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));
        harness.setHand(player1, List.of(new TimelessWitness()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Eternalize exiles the source and creates a 4/4 black Zombie token copy")
    void eternalizeCreatesTokenCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TimelessWitness()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Timeless Witness");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Timeless Witness"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Timeless Witness")
                        && permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.HUMAN, CardSubtype.SHAMAN);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }
}
