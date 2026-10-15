package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LumberingFalls;
import com.github.laxika.magicalvibes.cards.o.OrazcaRelic;
import com.github.laxika.magicalvibes.cards.p.ParadiseDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BagOfDevouring.class, ParadiseDruid.class, OrazcaRelic.class, LumberingFalls.class})
class BagOfDevouringTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another nontoken creature exiles it with the Bag and draws a card")
    void sacrificesCreatureExilesItAndDraws() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfDevouring());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ParadiseDruid());
        Card drawn = new ParadiseDruid();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getCard().getId()).sourcePermanentId()).isEqualTo(bag.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawn.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bag);
    }

    @Test
    @DisplayName("Sacrificing the Bag rolls a d10 and returns up to that many tracked cards")
    void sacrificeBagReturnsChosenTrackedCards() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfDevouring());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new ParadiseDruid());
        harness.setLibrary(player1, List.of(new ParadiseDruid()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        bag.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(sacrificed.getCard().getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sacrificed.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bag.getCard().getId()));
        assertThat(gd.getCardsExiledByPermanent(bag.getId())).isEmpty();
    }

    @Test
    void sacrificesNoncreatureArtifactAndCanReturnNoCards() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfDevouring());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OrazcaRelic());
        harness.setLibrary(player1, List.of(new ParadiseDruid()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(artifact.getCard().getId()).sourcePermanentId()).isEqualTo(bag.getId());
        bag.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(artifact.getCard().getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(artifact.getCard().getId()));
        assertThat(gd.findExiledCard(bag.getCard().getId())).isNull();
        harness.assertInGraveyard(player1, "Bag of Devouring");
    }

    @Test
    void sacrificingBagWithNoExiledCardsNeedsNoChoice() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfDevouring());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(bag.getCard().getId())).isNull();
        harness.assertInGraveyard(player1, "Bag of Devouring");
    }

    @Test
    void exilesSacrificedLandThatWasAnimatedAsCreature() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfDevouring());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new LumberingFalls());
        harness.setLibrary(player1, List.of(new ParadiseDruid()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.findExiledCard(land.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(land.getCard().getId()).sourcePermanentId()).isEqualTo(bag.getId());
        harness.assertNotInGraveyard(player1, "Lumbering Falls");
        harness.assertInHand(player1, "Paradise Druid");
    }
}
