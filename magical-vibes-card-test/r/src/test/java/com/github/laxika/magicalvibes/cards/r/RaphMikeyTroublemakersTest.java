package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FblthpTheLost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphMikeyTroublemakers.class, FountainOfYouth.class, GrizzlyBears.class, ChandraNalaar.class, FblthpTheLost.class})
class RaphMikeyTroublemakersTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals a creature, offers all legal attack destinations, and puts it tapped and attacking")
    void revealsCreatureAndChoosesAttackDestination() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new RaphMikeyTroublemakers());
        Permanent planeswalker = addTestPlaneswalker(player2, 4);
        Permanent battle = addTestBattle(player1, player2);
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        assertThat(choice.validPermanentIds()).contains(planeswalker.getId(), battle.getId());

        harness.handlePermanentChosen(player1, battle.getId());
        resolveAllTriggers();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isAttacking()).isTrue();
        assertThat(creature.getAttackTarget()).isEqualTo(battle.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Fountain of Youth");
    }

    @Test
    @DisplayName("An empty library does not put a creature onto the battlefield")
    void emptyLibrary() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new RaphMikeyTroublemakers());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A library with no creature returns every revealed card to the library")
    void noCreatureInLibrary() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new RaphMikeyTroublemakers());
        Card first = new FountainOfYouth();
        Card second = new FountainOfYouth();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Stops at the first creature and keeps unrevealed cards above the revealed remainder")
    void stopsAtFirstCreatureAndAttacksPlayer() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new RaphMikeyTroublemakers());
        Card revealed = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        Card unrevealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed, creature, unrevealed));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.getCard()).isSameAs(creature);
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, revealed);
    }

    @Test
    @DisplayName("A creature revealed from the library retains its origin for its enter ability")
    void revealedCreatureRetainsLibraryOrigin() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new RaphMikeyTroublemakers());
        Card firstDraw = new FountainOfYouth();
        Card secondDraw = new FountainOfYouth();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FblthpTheLost(), firstDraw, secondDraw));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addTestPlaneswalker(Player player, int loyalty) {
        ChandraNalaar card = new ChandraNalaar();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }

    private Permanent addTestBattle(Player controller, Player protector) {
        Card card = new Card();
        card.setName("Test Battle");
        card.setType(CardType.BATTLE);
        Permanent battle = new Permanent(card);
        battle.setCounterCount(CounterType.DEFENSE, 5);
        battle.setProtectorPlayerId(protector.getId());
        gd.playerBattlefields.get(controller.getId()).add(battle);
        return battle;
    }
}
