package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HundredTalonKami;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InameAsOneTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, the ETB may put a Spirit permanent from the library onto the battlefield")
    void handCastSearchesForSpiritPermanent() {
        Card spirit = new KamiOfOldStone();
        harness.setHand(player1, List.of(new InameAsOne()));
        harness.setLibrary(player1, List.of(spirit, new GrizzlyBears()));
        addInameMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Kami of Old Stone");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The hand-cast ETB does not search when Iname enters from a graveyard")
    void graveyardReturnDoesNotSearchLibrary() {
        InameAsOne iname = new InameAsOne();
        Card spirit = new KamiOfOldStone();
        harness.setGraveyard(player1, new ArrayList<>(List.of(iname)));
        harness.setHand(player1, List.of(new Zombify()));
        harness.setLibrary(player1, List.of(spirit));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, iname.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spirit);
        harness.assertOnBattlefield(player1, "Iname as One");
    }

    @Test
    @DisplayName("When Iname dies, it may exile itself and return a targeted Spirit permanent")
    void deathExilesSelfAndReturnsSpiritPermanent() {
        InameAsOne iname = new InameAsOne();
        Card spirit = new HundredTalonKami();
        harness.addToBattlefield(player1, iname);
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));
        castWrathOfGod();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(iname.getId()));
        harness.assertOnBattlefield(player1, "Hundred-Talon Kami");
        harness.assertNotInGraveyard(player1, "Iname as One");
    }

    @Test
    @DisplayName("The death trigger only targets Spirit permanent cards")
    void deathTriggerRejectsNonSpiritCards() {
        InameAsOne iname = new InameAsOne();
        Card nonSpirit = new GrizzlyBears();
        harness.addToBattlefield(player1, iname);
        harness.setGraveyard(player1, List.of(nonSpirit));
        castWrathOfGod();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(iname.getId());
        assertThat(choice.validCardIds()).doesNotContain(nonSpirit.getId());
        harness.assertInGraveyard(player1, "Iname as One");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addInameMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
    }

    private void castWrathOfGod() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
