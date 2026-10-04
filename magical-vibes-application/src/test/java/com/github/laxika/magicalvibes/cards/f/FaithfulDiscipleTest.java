package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AllThatGlitters;
import com.github.laxika.magicalvibes.cards.a.AngelicExaltation;
import com.github.laxika.magicalvibes.cards.a.AngelicGift;
import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.a.AuthorityOfTheConsuls;
import com.github.laxika.magicalvibes.cards.b.BanishingLight;
import com.github.laxika.magicalvibes.cards.c.CatharsCrusade;
import com.github.laxika.magicalvibes.cards.c.ClericClass;
import com.github.laxika.magicalvibes.cards.d.DivineVisitation;
import com.github.laxika.magicalvibes.cards.d.DuelistsHeritage;
import com.github.laxika.magicalvibes.cards.g.GauntletsOfLight;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.SigilOfTheEmptyThrone;
import com.github.laxika.magicalvibes.cards.s.SpectralSteel;
import com.github.laxika.magicalvibes.cards.t.TeleportationCircle;
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

@CardUsed({FaithfulDisciple.class, Shock.class, AnointedProcession.class, CatharsCrusade.class,
        AuthorityOfTheConsuls.class, SigilOfTheEmptyThrone.class, AllThatGlitters.class,
        BanishingLight.class, DivineVisitation.class, DuelistsHeritage.class, GloriousAnthem.class,
        GauntletsOfLight.class, TeleportationCircle.class, AngelicGift.class, SpectralSteel.class,
        ClericClass.class, AngelicExaltation.class})
class FaithfulDiscipleTest extends BaseCardTest {

    @Test
    void whenFaithfulDiscipleDiesItOffersThreeSpellbookCardsAndPutsTheChoiceIntoHand() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new FaithfulDisciple());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, disciple.getId());
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        harness.assertInGraveyard(player1, "Faithful Disciple");
    }

    @Test
    void opponentControlledDiscipleDraftsForItsControllerWithoutUsingTheirLibrary() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new FaithfulDisciple());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new FaithfulDisciple()));
        var libraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, disciple.getId());
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
        harness.assertInGraveyard(player2, "Faithful Disciple");
    }

    @Test
    void draftingRequiresExactlyOneOfTheOfferedCards() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new FaithfulDisciple());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, disciple.getId());
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(choice.cards().get(0).getId(), choice.cards().get(1).getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2,
                List.of(choice.cards().getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class))
                .isEqualTo(choice);

        var drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
    }
}
