package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BolassCitadel;
import com.github.laxika.magicalvibes.cards.b.Bribery;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KioraBehemothBeckoner;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Wargate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FblthpTheLost.class, GrizzlyBears.class, Shock.class, Wargate.class,
        BolassCitadel.class, Bribery.class, KioraBehemothBeckoner.class})
class FblthpTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering from hand draws one card")
    void enteringFromHandDrawsOneCard() {
        Shock drawn = new Shock();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new FblthpTheLost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Entering from the library draws two cards")
    void enteringFromLibraryDrawsTwoCards() {
        FblthpTheLost fblthp = new FblthpTheLost();
        GrizzlyBears drawn1 = new GrizzlyBears();
        GrizzlyBears drawn2 = new GrizzlyBears();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(fblthp, drawn1, drawn2, remaining));
        harness.setHand(player1, List.of(new Wargate()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 2);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player1, search.params().cards().indexOf(fblthp));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card == drawn1 || card == drawn2 || card == remaining);
    }

    @Test
    @DisplayName("Becoming the target of a spell shuffles Fblthp into its owner's library")
    void becomingTargetOfSpellShufflesIntoOwnersLibrary() {
        Permanent fblthp = harness.addToBattlefieldAndReturn(player1, new FblthpTheLost());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, fblthp.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(fblthp.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(
                card -> card.getId().equals(fblthp.getCard().getId()));
    }

    @Test
    @DisplayName("Casting from the library draws two cards instead of one")
    void castingFromLibraryDrawsTwoCards() {
        harness.addToBattlefield(player1, new BolassCitadel());
        KioraBehemothBeckoner drawn1 = new KioraBehemothBeckoner();
        KioraBehemothBeckoner drawn2 = new KioraBehemothBeckoner();
        BolassCitadel remaining = new BolassCitadel();
        harness.setLibrary(player1, List.of(new FblthpTheLost(), drawn1, drawn2, remaining));

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn1, drawn2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertOnBattlefield(player1, "Fblthp, the Lost");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The library bonus still draws two cards after Fblthp leaves")
    void libraryBonusSurvivesLeavingBattlefield() {
        harness.addToBattlefield(player1, new BolassCitadel());
        FblthpTheLost fblthp = new FblthpTheLost();
        harness.setLibrary(player1, List.of(fblthp, new KioraBehemothBeckoner(),
                new KioraBehemothBeckoner(), new BolassCitadel()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1);
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == fblthp).findFirst().orElseThrow();
        harness.castAndResolveInstant(player2, 0, permanent.getId());
        harness.assertNotOnBattlefield(player1, "Fblthp, the Lost");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Fblthp, the Lost");
    }

    @Test
    @DisplayName("Being targeted by an activated ability does not shuffle Fblthp")
    void activatedAbilityDoesNotTriggerShuffle() {
        Permanent fblthp = harness.addToBattlefieldAndReturn(player1, new FblthpTheLost());
        fblthp.setTapped(true);
        harness.addToBattlefield(player1, new KioraBehemothBeckoner());

        harness.activateAbility(player1, 1, null, fblthp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fblthp);
        assertThat(fblthp.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card == fblthp.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering from an opponent's library draws only one card")
    void enteringFromOpponentsLibraryDrawsOneCard() {
        FblthpTheLost fblthp = new FblthpTheLost();
        KioraBehemothBeckoner drawn = new KioraBehemothBeckoner();
        BolassCitadel remaining = new BolassCitadel();
        harness.setLibrary(player2, List.of(fblthp));
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setHand(player1, List.of(new Bribery()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fblthp, the Lost");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
