package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SultaiScavenger.class, AlpineGrizzly.class})
class SultaiScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new SultaiScavenger(), new SultaiScavenger(), new SultaiScavenger(),
                new SultaiScavenger(), new SultaiScavenger());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SultaiScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SultaiScavenger);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SultaiScavenger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new AlpineGrizzly());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayEntireCostWithManaWithoutDelving() {
        Card graveyardCard = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new SultaiScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sultai Scavenger");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void canCombineManaAndDelveLeavingUnselectedCardsInGraveyard() {
        Card first = new AlpineGrizzly();
        Card retained = new AlpineGrizzly();
        Card last = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(first, retained, last));
        harness.setHand(player1, List.of(new SultaiScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 2));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, last);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sultai Scavenger");
    }

    @Test
    void delveCannotPayTheBlackManaRequirement() {
        List<Card> graveyard = List.of(new AlpineGrizzly(), new AlpineGrizzly(),
                new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SultaiScavenger()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotExileMoreCardsThanTheGenericCost() {
        List<Card> graveyard = List.of(new AlpineGrizzly(), new AlpineGrizzly(),
                new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SultaiScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotCountTheSameGraveyardCardTwiceForDelve() {
        Card graveyardCard = new AlpineGrizzly();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new SultaiScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void flyingCreatureCanBlockSultaiScavenger() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SultaiScavenger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new SultaiScavenger());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isBlocking()).isTrue();
    }
}
