package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.s.ShelteringBoughs;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.cards.v.VelaTheNightClad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumingTide.class, SporeCrawler.class, Island.class, ShelteringBoughs.class, VelaTheNightClad.class, ControlMagic.class})
class ConsumingTideTest extends BaseCardTest {

    @Test
    void eachPlayerKeepsOneNonlandPermanentAndTheRestReturnToHand() {
        Permanent player1Kept = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent player1Returned = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent player2Kept = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());
        Permanent player2Returned = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());
        Island player2HandCard = new Island();

        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of(player2HandCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(player1Kept.getId(), player1Returned.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Kept.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2Kept.getId(), player2Returned.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Kept.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Kept, player1Land)
                .doesNotContain(player1Returned);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Kept)
                .doesNotContain(player2Returned);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Returned.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2HandCard, player2Returned.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawWhenNoOpponentHasMoreCardsInHand() {
        Permanent player1Kept = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent player1Returned = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent player2Kept = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());
        Permanent player2Returned = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());

        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Kept.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Kept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Kept)
                .doesNotContain(player1Returned);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Kept)
                .doesNotContain(player2Returned);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Returned.getCard());
    }

    @Test
    void resolvesWithOnlyLandsAndStillDrawsForLargerOpponentHand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void automaticallyKeepsTheOnlyNonlandPermanentForEachPlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());
        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void comparesHandsAfterReturningPermanentsAndSkipsPlayersWithoutNonlands() {
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent returned = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(kept.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returned.getCard());
    }

    @Test
    @CardUsed({ConsumingTide.class, SporeCrawler.class, VelaTheNightClad.class})
    void departingVelaSeesTheOtherCreatureReturnedAtTheSameTime() {
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent vela = harness.addToBattlefieldAndReturn(player1, new VelaTheNightClad());
        Permanent returned = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(kept.getId()));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(
                vela.getCard(), returned.getCard());
        harness.assertLife(player2, 18);
    }

    @Test
    void returnsAnUnchosenAuraToHandRatherThanTheGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShelteringBoughs());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new ConsumingTide()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId(), aura.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura.getCard());
        harness.assertNotInGraveyard(player1, "Sheltering Boughs");
    }

    @Test
    void returnsAControlledOpponentsCreatureToItsOwnerBeforeComparingHands() {
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());
        gd.playerBattlefields.get(player2.getId()).remove(stolen);
        gd.playerBattlefields.get(player1.getId()).add(stolen);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        Permanent control = harness.addToBattlefieldAndReturn(player1, new ControlMagic());
        control.setAttachedTo(stolen.getId());
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new ConsumingTide()));
        Island opponentHandCard = new Island();
        harness.setHand(player2, List.of(opponentHandCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(kept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(stolen.getCard(), opponentHandCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawn, control.getCard());
    }
}
