package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnOfTheMoleMan.class, Forest.class, GrizzlyBears.class, Shock.class})
class ReturnOfTheMoleManTest extends BaseCardTest {

    @Test
    void landfallMayMillTwoCards() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        Card first = new GrizzlyBears();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void decliningLandfallMillLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        Card card = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificeCreatesOneMoloidPerPermanentCardInGraveyard() {
        harness.addToBattlefield(player1, new ReturnOfTheMoleMan());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Moloid")))
                .hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card ->
                card.getName().equals("Return of the Mole Man"));
    }
}
