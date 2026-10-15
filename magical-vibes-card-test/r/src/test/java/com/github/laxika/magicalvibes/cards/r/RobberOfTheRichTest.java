package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RobberOfTheRich.class, Divination.class, Island.class})
class RobberOfTheRichTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the larger-hand opponent exiles their top card and permits casting it with any color")
    void attacksExileAndPermitCastingTopCard() {
        Permanent robber = addCreatureReady(player1, new RobberOfTheRich());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island(), new Island()));
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isFalse();
        assertThat(entry.sourcePermanentId()).isEqualTo(robber.getId());
        assertThat(entry.ownerId()).isEqualTo(player2.getId());

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The attack trigger does not fire when the defending player does not have the larger hand")
    void noExileWhenDefendingPlayerDoesNotHaveMoreCardsInHand() {
        harness.addToBattlefield(player1, new RobberOfTheRich());
        harness.setHand(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Island()));
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(List.of(0));

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).contains(topCard);
    }

    @Test
    void canCastAfterRobberLeavesTheBattlefield() {
        Card stolenCard = exileCreatureWithRobber();
        Permanent robber = findPermanent(player1, "Robber of the Rich");
        robber.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(robber);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, stolenCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(stolenCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(stolenCard.getId()));
    }

    @Test
    void canCastOnALaterTurnAfterAttackingWithARogue() {
        Card stolenCard = exileCreatureWithRobber();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, stolenCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(stolenCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(stolenCard.getId()));
    }

    @Test
    void originalTriggerControllerRetainsPermissionAfterControlChanges() {
        Card stolenCard = exileCreatureWithRobber();
        Permanent robber = findPermanent(player1, "Robber of the Rich");
        gd.playerBattlefields.get(player1.getId()).remove(robber);
        gd.playerBattlefields.get(player2.getId()).add(robber);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, stolenCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(stolenCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(stolenCard.getId()));
    }

    @Test
    void cannotCastOnALaterTurnBeforeAttackingWithARogue() {
        Card stolenCard = exileCreatureWithRobber();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(
                () -> harness.castFromExile(player1, stolenCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolenCard.getId())).isNotNull();
    }

    @Test
    void handSizeConditionIsCheckedAgainOnResolution() {
        addCreatureReady(player1, new RobberOfTheRich());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        Card topCard = new RobberOfTheRich();
        harness.setLibrary(player2, List.of(topCard));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).contains(topCard);
    }

    @Test
    void cannotPlayALandExiledByRobber() {
        addCreatureReady(player1, new RobberOfTheRich());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        Card land = new Island();
        harness.setLibrary(player2, List.of(land));

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
    }

    private Card exileCreatureWithRobber() {
        addCreatureReady(player1, new RobberOfTheRich());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        Card stolenCard = new RobberOfTheRich();
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.setLibrary(player2, List.of(stolenCard, new Island(), new Island(), new Island(), new Island()));

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.findExiledCard(stolenCard.getId())).isNotNull();
        return stolenCard;
    }
}
