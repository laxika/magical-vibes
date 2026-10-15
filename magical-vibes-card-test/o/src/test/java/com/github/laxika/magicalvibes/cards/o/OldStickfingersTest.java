package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldStickfingers.class, CandlegroveWitch.class, Plains.class})
class OldStickfingersTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger mills creature cards until X are found and bottoms the rest")
    void castTriggerMillsCreaturesAndBottomsNoncreatures() {
        Card firstNoncreature = new Plains();
        Card firstCreature = new CandlegroveWitch();
        Card secondNoncreature = new Plains();
        Card secondCreature = new CandlegroveWitch();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(
                firstNoncreature, firstCreature, secondNoncreature, secondCreature, untouched));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Old Stickfingers");
        harness.assertInGraveyard(player1, "Candlegrove Witch");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCreature, secondCreature);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.getFirst()).isSameAs(untouched);
        assertThat(library.subList(1, library.size())).containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);

        Permanent oldStickfingers = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Old Stickfingers"));
        assertThat(gqs.getEffectivePower(gd, oldStickfingers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldStickfingers)).isEqualTo(2);
    }

    @Test
    @DisplayName("If the library ends first, every revealed creature still goes to the graveyard")
    void libraryCanEndBeforeXCreaturesAreFound() {
        Card noncreature = new Plains();
        Card creature = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(noncreature, creature));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Old Stickfingers");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(noncreature);
    }

    @Test
    @DisplayName("X=0 reveals nothing and the creature's power equals existing creature cards in the graveyard")
    void xZeroRevealsNothing() {
        Card libraryCard = new Plains();
        Card graveyardCreature = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Old Stickfingers");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        Permanent oldStickfingers = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Old Stickfingers"));
        assertThat(gqs.getEffectivePower(gd, oldStickfingers)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oldStickfingers)).isEqualTo(1);
    }
    @Test
    @DisplayName("The cast trigger resolves before Old Stickfingers enters the battlefield")
    void castTriggerResolvesBeforeCreatureSpell() {
        Card creature = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Old Stickfingers");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Old Stickfingers");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Old Stickfingers");
    }

    @Test
    @DisplayName("Power and toughness track only creature cards in the controller's graveyard")
    void powerAndToughnessUpdateWithControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new CandlegroveWitch(), new Plains()));
        harness.setGraveyard(player2, List.of(new CandlegroveWitch(), new CandlegroveWitch()));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new OldStickfingers());

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new CandlegroveWitch(), new CandlegroveWitch(), new Plains()));
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Plains()));
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Old Stickfingers");
        harness.assertInGraveyard(player1, "Old Stickfingers");
    }

    @Test
    @DisplayName("An empty library and no creature cards in the graveyard cause Old Stickfingers to die")
    void emptyLibraryLeavesZeroToughnessCreature() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new OldStickfingers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Old Stickfingers");
        harness.assertInGraveyard(player1, "Old Stickfingers");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The characteristic-defining ability works in hand and counts Old Stickfingers itself in the graveyard")
    void powerAndToughnessWorkOutsideBattlefield() {
        Card stickfingers = new OldStickfingers();
        Card creature = new CandlegroveWitch();
        harness.setHand(player1, List.of(stickfingers));
        harness.setGraveyard(player1, List.of(creature, new Plains()));

        assertThat(gqs.getEffectiveCardPower(gd, stickfingers)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, stickfingers)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(creature, stickfingers, new Plains()));

        assertThat(gqs.getEffectiveCardPower(gd, stickfingers)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, stickfingers)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering without being cast does not reveal cards")
    void enteringWithoutCastingDoesNotTriggerReveal() {
        Card libraryCreature = new CandlegroveWitch();
        Card graveyardCreature = new CandlegroveWitch();
        harness.setLibrary(player1, List.of(libraryCreature));
        harness.setGraveyard(player1, List.of(graveyardCreature));

        harness.enterBattlefieldAndReturn(player1, new OldStickfingers());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCreature);
        harness.assertOnBattlefield(player1, "Old Stickfingers");
    }
}
