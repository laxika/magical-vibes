package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KozilekButcherOfTruth.class, GrizzlyBears.class, Forest.class, KeeningStone.class})
class KozilekButcherOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Kozilek draws four cards")
    void castingDrawsFourCards() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new KozilekButcherOfTruth()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking with Kozilek makes the defending player sacrifice four permanents")
    void annihilatorFour() {
        Permanent kozilek = addCreatureReady(player1, new KozilekButcherOfTruth());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kozilek)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Kozilek goes to a graveyard, its owner's graveyard is shuffled into their library")
    void shufflesItsOwnersGraveyardIntoLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent kozilek = addCreatureReady(player1, new KozilekButcherOfTruth());
        kozilek.setMarkedDamage(13);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).extracting(Card::getName)
                .containsExactlyInAnyOrder("Kozilek, Butcher of Truth", "Grizzly Bears");
    }

    @Test
    @DisplayName("The cast trigger draws before Kozilek enters the battlefield")
    void drawResolvesBeforeCreatureSpell() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new KozilekButcherOfTruth()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Kozilek, Butcher of Truth")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Entering without being cast does not draw cards")
    void enteringWithoutCastingDoesNotDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new KozilekButcherOfTruth());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(countPermanents(player1, "Kozilek, Butcher of Truth")).isEqualTo(1);
    }

    @Test
    @DisplayName("Annihilator sacrifices all available permanents when there are fewer than four")
    void annihilatorWithFewerThanFourPermanents() {
        addCreatureReady(player1, new KozilekButcherOfTruth());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Kozilek, Butcher of Truth")).isEqualTo(1);
    }

    @Test
    @DisplayName("The defending player chooses which four permanents to sacrifice")
    void defendingPlayerChoosesSacrifices() {
        addCreatureReady(player1, new KozilekButcherOfTruth());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        List<Permanent> lands = List.copyOf(gd.playerBattlefields.get(player2.getId()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2,
                lands.subList(1, 5).stream().map(Permanent::getId).toList());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(lands.getFirst());
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Milling Kozilek leaves it in the graveyard until the shuffle trigger resolves")
    void millingTriggersOwnersGraveyardShuffle() {
        KozilekButcherOfTruth kozilek = new KozilekButcherOfTruth();
        Forest graveyardCard = new Forest();
        Forest libraryCard = new Forest();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player2, List.of(kozilek, libraryCard));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new KeeningStone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard, kozilek);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(graveyardCard, kozilek, libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
