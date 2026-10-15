package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SparkDouble;
import com.github.laxika.magicalvibes.cards.t.TazeemRoilmage;
import com.github.laxika.magicalvibes.cards.t.TeferisAgelessInsight;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JaceMirrorMage.class, TazeemRoilmage.class, Island.class, SparkDouble.class, TeferisAgelessInsight.class})
class JaceMirrorMageTest extends BaseCardTest {

    @Test
    void kickedEntryCreatesNonLegendaryTokenWithOneLoyalty() {
        harness.setHand(player1, List.of(new JaceMirrorMage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(token.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void plusOneScriesTwo() {
        Permanent jace = addReadyJace(player1, 4);
        TazeemRoilmage top = new TazeemRoilmage();
        Island bottom = new Island();
        harness.setLibrary(player1, List.of(top, bottom));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
    }

    @Test
    void zeroAbilityDrawsAndRemovesLoyaltyEqualToDrawnManaValue() {
        Permanent jace = addReadyJace(player1, 5);
        TazeemRoilmage drawn = new TazeemRoilmage();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void unkickedEntryDoesNotCreateToken() {
        harness.setHand(player1, List.of(new JaceMirrorMage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawingLandRevealsItWithoutRemovingLoyalty() {
        Permanent jace = addReadyJace(player1, 1);
        Island drawn = new Island();
        harness.setLibrary(player1, List.of(drawn));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals Island"));
    }

    @Test
    void drawingCardWorthMoreThanRemainingLoyaltyPutsJaceInGraveyard() {
        addReadyJace(player1, 1);
        TazeemRoilmage drawn = new TazeemRoilmage();
        harness.setLibrary(player1, List.of(drawn));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.assertNotOnBattlefield(player1, "Jace, Mirror Mage");
        harness.assertInGraveyard(player1, "Jace, Mirror Mage");
    }

    @Test
    void replacedDrawDoesNotRevealCardsOrRemoveLoyalty() {
        Permanent jace = addReadyJace(player1, 4);
        harness.addToBattlefield(player1, new TeferisAgelessInsight());
        TazeemRoilmage first = new TazeemRoilmage();
        TazeemRoilmage second = new TazeemRoilmage();
        harness.setLibrary(player1, List.of(first, second));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains("reveals"));
    }

    @Test
    void copyingKickerTokenPreservesOneStartingLoyalty() {
        harness.setHand(player1, List.of(new JaceMirrorMage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        SparkDouble copy = new SparkDouble();
        harness.setHand(player1, List.of(copy));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent copiedToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(copy.getId()))
                .findFirst().orElseThrow();
        assertThat(copiedToken.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(copiedToken.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent jace = harness.addToBattlefieldAndReturn(player, new JaceMirrorMage());
        jace.setCounterCount(CounterType.LOYALTY, loyalty);
        jace.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }
}
