package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.b.BloodhunterBat;
import com.github.laxika.magicalvibes.cards.c.CauldronFamiliar;
import com.github.laxika.magicalvibes.cards.c.CurseOfLeeches;
import com.github.laxika.magicalvibes.cards.c.CruelReality;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.s.SorcerersBroom;
import com.github.laxika.magicalvibes.cards.t.TormentOfScarabs;
import com.github.laxika.magicalvibes.cards.t.TrespassersCurse;
import com.github.laxika.magicalvibes.cards.u.UnwillingIngredient;
import com.github.laxika.magicalvibes.cards.w.WitchsCauldron;
import com.github.laxika.magicalvibes.cards.w.WitchsCottage;
import com.github.laxika.magicalvibes.cards.w.WitchsFamiliar;
import com.github.laxika.magicalvibes.cards.w.WitchsOven;
import com.github.laxika.magicalvibes.cards.w.WitchsVengeance;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseboundWitch.class, WitchsCauldron.class, WitchsCottage.class,
        CauldronFamiliar.class, BloodhunterBat.class, CruelReality.class, WitchsVengeance.class,
        WitchsFamiliar.class, BlackCat.class, UnwillingIngredient.class, TormentOfScarabs.class,
        WitchsOven.class, CurseOfLeeches.class, SorcerersBroom.class, ExpandedAnatomy.class,
        TrespassersCurse.class, Shock.class})
class CurseboundWitchTest extends BaseCardTest {

    @Test
    void whenCurseboundWitchDiesItOffersThreeSpellbookCardsAndPutsTheChoiceIntoHand() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CurseboundWitch());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, witch.getId());
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        harness.assertInGraveyard(player1, "Cursebound Witch");
    }

    @Test
    void opponentControlledWitchDraftsOnlyForItsControllerWithoutUsingTheirLibrary() {
        Permanent witch = harness.addToBattlefieldAndReturn(player2, new CurseboundWitch());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new CurseboundWitch()));
        var libraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, witch.getId());
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards().stream().map(card -> card.getName()).toList()).doesNotHaveDuplicates();
        var drafted = choice.cards().getLast();

        harness.handleMultipleCardsChosen(player2, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drafted);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(libraryBefore);
        harness.assertInGraveyard(player2, "Cursebound Witch");
    }

    @Test
    void draftIsMandatoryAndRejectsChoosingMultipleCards() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new CurseboundWitch());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, witch.getId());
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(choice.cards().get(0).getId(), choice.cards().get(1).getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isEqualTo(choice);

        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }
}
