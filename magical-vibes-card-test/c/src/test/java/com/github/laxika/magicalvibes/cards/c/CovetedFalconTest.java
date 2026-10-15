package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GraniteWitness;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovetedFalcon.class, GraniteWitness.class, Island.class, Shock.class})
class CovetedFalconTest extends BaseCardTest {

    @Test
    void turningFaceUpGivesOpponentAnyNumberOfPermanentsAndDrawsForEach() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        Permanent falcon = castFaceDown();
        int handSizeBeforeDraw = gd.playerHands.get(player1.getId()).size();

        turnFaceUp(falcon);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(first.getId(), second.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeDraw + 2);
    }

    @Test
    void attacksToGainControlOfPermanentItsOwnerDoesNotControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        Permanent falcon = castFaceDown();
        turnFaceUp(falcon);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        falcon.setSummoningSick(false);

        int falconIndex = gd.playerBattlefields.get(player1.getId()).indexOf(falcon);
        declareAttackers(player1, List.of(falconIndex));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
    }

    @Test
    void turningFaceUpCanGiveAwayNoPermanents() {
        Permanent falcon = castFaceDown();
        int handSize = gd.playerHands.get(player1.getId()).size();

        turnFaceUp(falcon);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(falcon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(falcon);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void turningFaceUpCanGiveAwayFalconItselfAndDrawForItsFormerController() {
        Permanent falcon = castFaceDown();
        harness.setLibrary(player1, List.of(new CovetedFalcon()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        turnFaceUp(falcon);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, falcon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(falcon);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(falcon);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void drawsOnlyForPermanentsStillLegalWhenTurnFaceUpAbilityResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        Permanent falcon = castFaceDown();
        harness.setLibrary(player1, List.of(new CovetedFalcon(), new CovetedFalcon()));

        turnFaceUp(falcon);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, first.getId());
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        harness.assertInGraveyard(player1, "Granite Witness");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void disguiseWardCountersOpponentsSpellWhenTheyDeclineToPay() {
        Permanent falcon = castFaceDown();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, falcon.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(falcon);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void turningFaceUpCanGiveAwayMoreThanNinetyNinePermanents() {
        List<Permanent> lands = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Island()))
                .toList();
        Permanent falcon = castFaceDown();
        harness.setLibrary(player1, IntStream.range(0, 100)
                .mapToObj(i -> new CovetedFalcon()).toList());
        int handSize = gd.playerHands.get(player1.getId()).size();

        turnFaceUp(falcon);
        harness.handlePermanentChosen(player1, player2.getId());
        for (Permanent land : lands) {
            harness.handlePermanentChosen(player1, land.getId());
        }
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsAll(lands);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(falcon);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 100);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new CovetedFalcon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Coveted Falcon");
    }

    private void turnFaceUp(Permanent falcon) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(falcon));
    }
}
