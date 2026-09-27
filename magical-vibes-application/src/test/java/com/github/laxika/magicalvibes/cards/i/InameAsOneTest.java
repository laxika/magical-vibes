package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HundredTalonKami;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.s.SoullessRevival;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InameAsOne.class, KamiOfOldStone.class, GrizzlyBears.class, Zombify.class,
        HundredTalonKami.class, WrathOfGod.class, SoullessRevival.class})
class InameAsOneTest extends BaseCardTest {

    @Test
    @DisplayName("When cast from hand, the ETB may put a Spirit permanent from the library onto the battlefield")
    void handCastSearchesForSpiritPermanent() {
        Card spirit = new KamiOfOldStone();
        harness.setLibrary(player1, List.of(spirit, new GrizzlyBears()));
        harness.castFromHand(player1, new InameAsOne(), "{8}{B}{B}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

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
    @DisplayName("The hand-cast ETB may be declined")
    void handCastMayDeclineLibrarySearch() {
        Card spirit = new KamiOfOldStone();
        Card nonSpirit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(spirit, nonSpirit));
        harness.castFromHand(player1, new InameAsOne(), "{8}{B}{B}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spirit, nonSpirit);
        harness.assertNotOnBattlefield(player1, "Kami of Old Stone");
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
    @DisplayName("The death trigger can target Iname itself, but exiling it makes that target unavailable")
    void deathTriggerSelfTargetIsRemovedBeforeReturn() {
        InameAsOne iname = new InameAsOne();
        Card spirit = new HundredTalonKami();
        harness.addToBattlefield(player1, iname);
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));
        castWrathOfGod();

        harness.handleMultipleCardsChosen(player1, List.of(iname.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(iname.getId()));
        harness.assertNotInGraveyard(player1, "Iname as One");
        harness.assertInGraveyard(player1, "Hundred-Talon Kami");
        harness.assertNotOnBattlefield(player1, "Iname as One");
    }

    @Test
    @DisplayName("The death trigger may be declined")
    void deathTriggerMayBeDeclined() {
        InameAsOne iname = new InameAsOne();
        Card spirit = new HundredTalonKami();
        harness.addToBattlefield(player1, iname);
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));
        castWrathOfGod();

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Iname as One");
        harness.assertInGraveyard(player1, "Hundred-Talon Kami");
    }

    @Test
    @DisplayName("The death trigger does nothing if Iname leaves the graveyard before resolution")
    void deathTriggerDoesNothingIfSourceLeavesGraveyard() {
        InameAsOne iname = new InameAsOne();
        Card spirit = new HundredTalonKami();
        harness.addToBattlefield(player1, iname);
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));
        castWrathOfGod();

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.setHand(player1, List.of(new SoullessRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, iname.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Iname as One");
        harness.assertInGraveyard(player1, "Hundred-Talon Kami");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Iname as One");
        harness.assertInGraveyard(player1, "Hundred-Talon Kami");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
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

    private void castWrathOfGod() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
