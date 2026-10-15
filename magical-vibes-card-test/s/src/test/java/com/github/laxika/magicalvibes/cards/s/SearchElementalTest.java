package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DemonicCounsel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YawgmothDemon;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearchElemental.class, DemonicCounsel.class, GrizzlyBears.class, YawgmothDemon.class,
        ZhalfirinVoid.class})
class SearchElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Searching your library triggers scry 1")
    void searchingYourLibraryTriggersScry() {
        harness.addToBattlefield(player1, new SearchElemental());
        Card searchedCard = new YawgmothDemon();
        harness.setLibrary(player1, List.of(searchedCard, new GrizzlyBears()));
        harness.setHand(player1, List.of(new DemonicCounsel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }

    @Test
    @DisplayName("Scrying puts a counter on Search Elemental and makes it unblockable this turn")
    void scryingPutsCounterAndMakesUnblockable() {
        Permanent elemental = readyPermanent(player1, new SearchElemental());
        Permanent blocker = readyPermanent(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new ZhalfirinVoid());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        elemental.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(elemental)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private Permanent readyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Scrying an empty library still triggers Search Elemental")
    void scryingEmptyLibraryTriggers() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new SearchElemental());
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new ZhalfirinVoid());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each scry triggers every Search Elemental, including when bottoming no cards")
    void repeatedScryTriggersEachElemental() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SearchElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SearchElemental());
        harness.setLibrary(player1, List.of(new SearchElemental()));

        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new ZhalfirinVoid());
            harness.passBothPriorities();
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's scry does not trigger Search Elemental")
    void opponentsScryDoesNotTrigger() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new SearchElemental());
        harness.setLibrary(player2, List.of(new SearchElemental()));

        harness.enterBattlefieldAndReturn(player2, new ZhalfirinVoid());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Searching an empty library still causes scry and the counter trigger")
    void searchingEmptyLibraryTriggersBothAbilities() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new SearchElemental());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DemonicCounsel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's library search does not trigger Search Elemental")
    void opponentsSearchDoesNotTrigger() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new SearchElemental());
        harness.setLibrary(player2, List.of(new YawgmothDemon()));
        harness.setHand(player2, List.of(new DemonicCounsel()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter persists but the blocking restriction expires at end of turn")
    void blockingRestrictionExpiresButCounterRemains() {
        Permanent elemental = readyPermanent(player1, new SearchElemental());
        Permanent blocker = readyPermanent(player2, new SearchElemental());
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new ZhalfirinVoid());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        elemental.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(elemental))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
