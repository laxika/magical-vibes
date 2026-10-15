package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DrunauCorpseTrawler;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LamplighterOfSelhoff.class, DrunauCorpseTrawler.class, DevilthornFox.class, Forest.class})
class LamplighterOfSelhoffTest extends BaseCardTest {

    @Test
    @DisplayName("With another Zombie, the ETB may draw then discard")
    void etbMayDrawAndDiscardWithAnotherZombie() {
        harness.addToBattlefield(player1, new DrunauCorpseTrawler());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LamplighterOfSelhoff(), new DevilthornFox()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Devilthorn Fox");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Without another Zombie, the ETB does not trigger")
    void etbDoesNotTriggerWithoutAnotherZombie() {
        harness.setHand(player1, List.of(new LamplighterOfSelhoff()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Zombie does not satisfy the ETB condition")
    void opponentsZombieDoesNotSatisfyCondition() {
        harness.addToBattlefield(player2, new DrunauCorpseTrawler());
        harness.setHand(player1, List.of(new LamplighterOfSelhoff()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningDrawDoesNotDiscard() {
        harness.addToBattlefield(player1, new DrunauCorpseTrawler());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LamplighterOfSelhoff(), new DevilthornFox()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Devilthorn Fox");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conditionIsRecheckedWhenOtherZombieLeaves() {
        harness.addToBattlefield(player1, new DrunauCorpseTrawler());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LamplighterOfSelhoff(), new DevilthornFox()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Devilthorn Fox");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void canDiscardTheCardJustDrawnFromAnOtherwiseEmptyHand() {
        harness.addToBattlefield(player1, new DrunauCorpseTrawler());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LamplighterOfSelhoff()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherNonZombieDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new LamplighterOfSelhoff()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void abilityStillResolvesAfterLamplighterLeavesWithAnotherZombieRemaining() {
        harness.addToBattlefield(player1, new DrunauCorpseTrawler());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new LamplighterOfSelhoff()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
