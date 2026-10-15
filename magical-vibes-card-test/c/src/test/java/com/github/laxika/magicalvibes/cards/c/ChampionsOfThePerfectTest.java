package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChampionsOfThePerfect.class, ElvishMystic.class, GrizzlyBears.class, LightningBolt.class, AirElemental.class})
class ChampionsOfThePerfectTest extends BaseCardTest {

    @Test
    @DisplayName("Beholds an Elf and returns it to its owner's hand when Champions leaves")
    void beholdsElfAndReturnsItToHand() {
        Card beheldCard = new ElvishMystic();
        Permanent beheldPermanent = harness.addToBattlefieldAndReturn(player1, beheldCard);
        harness.setHand(player1, List.of(new ChampionsOfThePerfect()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, beheldPermanent.getId());
        harness.passBothPriorities();

        Permanent champions = findPermanent(player1, "Champions of the Perfect");
        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, champions));

        assertThat(gd.findExiledCard(beheldCard.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beheldCard);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(beheldCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(beheldCard);
    }

    @Test
    @DisplayName("Each creature spell cast afterwards draws a card")
    void drawsOnEachCreatureSpell() {
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        harness.setHand(player1, List.of(new ChampionsOfThePerfect()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, elf.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Noncreature spells cast afterwards do not draw")
    void noDrawForNoncreatureSpells() {
        harness.setLibrary(player1, List.of(new AirElemental()));
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        harness.setHand(player1, List.of(new ChampionsOfThePerfect()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreatureWithBeholdPermanent(player1, 0, elf.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An Elf in hand is exiled as a cost and Champions does not trigger on itself")
    void beholdsElfFromHandWithoutDrawingForItself() {
        Card elf = new ElvishMystic();
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ChampionsOfThePerfect(), elf));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);

        assertThat(gd.findExiledCard(elf.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Champions of the Perfect")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Champions cannot be cast without exiling an Elf")
    void cannotCastWithoutBeholdCost() {
        harness.setHand(player1, List.of(new ChampionsOfThePerfect()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-Elf in hand cannot pay the behold cost")
    void cannotBeholdNonElfCard() {
        Card bear = new GrizzlyBears();
        harness.setHand(player1, List.of(new ChampionsOfThePerfect(), bear));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreatureWithBeholdHandCard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bear.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing resolves before the creature spell")
    void drawTriggerResolvesBeforeCreature() {
        harness.addToBattlefield(player1, new ChampionsOfThePerfect());
        Card drawnCard = new LightningBolt();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    @DisplayName("An opponent's creature spell does not trigger a draw")
    void noDrawForOpponentsCreatureSpell() {
        harness.addToBattlefield(player1, new ChampionsOfThePerfect());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }
}
