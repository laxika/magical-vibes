package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RavenWings;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TibaltsTrickery.class, AxgardCavalry.class, RavenWings.class, Forest.class,
        VillageRites.class, ToskiBearerOfSecrets.class})
class TibaltsTrickeryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell, mills one to three cards, and offers the first different-name nonland")
    void countersMillsAndOffersDifferentNameCard() {
        AxgardCavalry target = new AxgardCavalry();
        RavenWings freeCast = new RavenWings();
        TibaltsTrickery trickery = new TibaltsTrickery();
        prepareTargetSpell(target);
        harness.setHand(player2, List.of(trickery));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new AxgardCavalry(), freeCast));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSizeBetween(2, 4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Axgard Cavalry");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(freeCast);
        assertThat(gd.getCardsExiledByPermanent(trickery.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the free cast counters the target and returns all exiled cards to the library")
    void decliningFreeCastBottomsExiledCards() {
        AxgardCavalry target = new AxgardCavalry();
        TibaltsTrickery trickery = new TibaltsTrickery();
        prepareTargetSpell(target);
        harness.setHand(player2, List.of(trickery));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new AxgardCavalry(), new RavenWings()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Axgard Cavalry");
        assertThat(gd.getCardsExiledByPermanent(trickery.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSizeBetween(2, 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("If no different-name nonland is found, the exiled cards are bottomed and the spell is countered")
    void noDifferentNameCardBottomsEverything() {
        AxgardCavalry target = new AxgardCavalry();
        TibaltsTrickery trickery = new TibaltsTrickery();
        prepareTargetSpell(target);
        harness.setHand(player2, List.of(trickery));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new AxgardCavalry()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Axgard Cavalry");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(trickery.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSizeBetween(1, 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not stop the target spell being countered")
    void emptyLibraryStillCountersTarget() {
        AxgardCavalry target = new AxgardCavalry();
        prepareTargetSpell(target);
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(new TibaltsTrickery()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An uncounterable spell still gives its controller the mill and free cast")
    void uncounterableSpellStillMillsAndOffersCard() {
        ToskiBearerOfSecrets target = new ToskiBearerOfSecrets();
        RavenWings freeCast = new RavenWings();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), freeCast));
        harness.setHand(player2, List.of(new TibaltsTrickery()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(target);
        assertThat(gd.stack.getLast().getCard()).isSameAs(freeCast);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSizeBetween(1, 3).doesNotContain(target);
    }

    @Test
    @DisplayName("A found card whose mandatory additional cost cannot be paid is bottomed too")
    void uncastableFoundCardReturnsToLibrary() {
        AxgardCavalry target = new AxgardCavalry();
        VillageRites found = new VillageRites();
        TibaltsTrickery trickery = new TibaltsTrickery();
        prepareTargetSpell(target);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), found));
        harness.setHand(player2, List.of(trickery));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).contains(found).hasSizeBetween(1, 3);
        assertThat(gd.getCardsExiledByPermanent(trickery.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareTargetSpell(Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
