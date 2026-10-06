package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonedDungeoneer.class, Forest.class, HillGiant.class})
class SeasonedDungeoneerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield takes the initiative and ventures into the Undercity")
    void takesInitiativeAndVenture() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new SeasonedDungeoneer());

        resolveAllTriggers();

        assertThat(gd.initiativePlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.UNDERCITY, 0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Whenever you attack, an eligible attacking creature can be chosen to explore")
    void attacksTriggerProtectionAndExplore() {
        Permanent dungeoneer = addCreatureReady(player1, new SeasonedDungeoneer());
        harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, dungeoneer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Exploring a nonland adds a counter and can leave the card on top")
    void exploresNonlandAndKeepsIt() {
        Permanent attacker = addCreatureReady(player1, new SeasonedDungeoneer());
        SeasonedDungeoneer revealed = new SeasonedDungeoneer();
        harness.setLibrary(player1, List.of(revealed));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(revealed);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(revealed);
    }

    @Test
    @DisplayName("Exploring a nonland can put it into the graveyard")
    void exploresNonlandAndBinsIt() {
        Permanent attacker = addCreatureReady(player1, new SeasonedDungeoneer());
        SeasonedDungeoneer revealed = new SeasonedDungeoneer();
        harness.setLibrary(player1, List.of(revealed));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(revealed);
    }

    @Test
    @DisplayName("Exploring an empty library still puts a counter on the attacker")
    void exploresEmptyLibrary() {
        Permanent attacker = addCreatureReady(player1, new SeasonedDungeoneer());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Protection prevents creatures from blocking the chosen attacker")
    void chosenAttackerCannotBeBlockedByCreatures() {
        Permanent attacker = addCreatureReady(player1, new SeasonedDungeoneer());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SeasonedDungeoneer());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("An attack without an eligible attacker does not explore")
    void ineligibleAttackerDoesNotExplore() {
        harness.addToBattlefieldAndReturn(player1, new SeasonedDungeoneer());
        addCreatureReady(player1, new HillGiant());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Attacking with multiple creatures explores only once per Dungeoneer")
    void multipleAttackersTriggerOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new SeasonedDungeoneer());
        addCreatureReady(player1, new HillGiant());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0, 1));
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("A target that stops attacking before resolution does not explore or gain protection")
    void targetMustStillBeAttackingOnResolution() {
        Permanent attacker = addCreatureReady(player1, new SeasonedDungeoneer());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SeasonedDungeoneer());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
