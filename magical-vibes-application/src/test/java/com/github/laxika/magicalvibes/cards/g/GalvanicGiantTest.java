package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.StormReading;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalvanicGiant.class, StormReading.class, CrawWurm.class, Forest.class, GrizzlyBears.class, Hurricane.class})
class GalvanicGiantTest extends BaseCardTest {

    @Test
    void highManaValueSpellTapsAndStunsOpposingCreature() {
        harness.addToBattlefield(player1, new GalvanicGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void lowerManaValueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GalvanicGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void adventureDrawsFourDiscardsTwoAndExilesTheCard() {
        GalvanicGiant card = new GalvanicGiant();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void chosenXCountsTowardTheFiveManaValueThreshold() {
        harness.addToBattlefield(player1, new GalvanicGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GalvanicGiant());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 4);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    void alreadyTappedCreatureStillReceivesAStunCounter() {
        harness.addToBattlefield(player1, new GalvanicGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GalvanicGiant());
        target.setTapped(true);
        harness.setHand(player1, List.of(new GalvanicGiant()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAdventure(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
    }

    @Test
    void opponentsAdventureDoesNotTrigger() {
        harness.addToBattlefield(player1, new GalvanicGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GalvanicGiant());
        harness.setHand(player2, List.of(new GalvanicGiant()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castAdventure(player2, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        GalvanicGiant card = new GalvanicGiant();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Galvanic Giant");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
