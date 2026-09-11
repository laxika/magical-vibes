package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MatterReshaper.class, WrathOfGod.class, Forest.class, SerraAngel.class})
class MatterReshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting Matter Reshaper's death trigger puts an eligible permanent onto the battlefield")
    void acceptsEligiblePermanent() {
        Forest forest = new Forest();
        setLibrary(forest);
        killMatterReshaper();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Declining Matter Reshaper's death trigger puts an eligible permanent into hand")
    void declinesEligiblePermanent() {
        Forest forest = new Forest();
        setLibrary(forest);
        killMatterReshaper();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Matter Reshaper puts a revealed nonpermanent card into hand")
    void putsNonpermanentIntoHand() {
        WrathOfGod wrathOfGod = new WrathOfGod();
        setLibrary(wrathOfGod);
        killMatterReshaper();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(wrathOfGod);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(wrathOfGod);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Matter Reshaper puts a revealed permanent with mana value greater than three into hand")
    void putsOverLimitPermanentIntoHand() {
        SerraAngel serraAngel = new SerraAngel();
        setLibrary(serraAngel);
        killMatterReshaper();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(serraAngel);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(serraAngel);
        harness.assertInGraveyard(player1, "Matter Reshaper");
    }

    private void killMatterReshaper() {
        harness.addToBattlefield(player1, new MatterReshaper());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
