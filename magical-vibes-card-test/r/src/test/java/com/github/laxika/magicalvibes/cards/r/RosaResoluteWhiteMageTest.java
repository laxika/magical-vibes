package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RosaResoluteWhiteMage.class, GrizzlyBears.class})
class RosaResoluteWhiteMageTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a counter on and grants lifelink to a creature you control")
    void beginningOfCombatCountersAndGrantsLifelink() {
        addCreatureReady(player1, new RosaResoluteWhiteMage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter remains while lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new RosaResoluteWhiteMage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addCreatureReady(player1, new RosaResoluteWhiteMage());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rosa can target herself and gains life from the resulting combat damage")
    void canTargetHerselfAndGainLifeFromCombatDamage() {
        Permanent rosa = addCreatureReady(player1, new RosaResoluteWhiteMage());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, rosa.getId());
        harness.passBothPriorities();

        assertThat(rosa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rosa, Keyword.LIFELINK)).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The combat ability still resolves after Rosa leaves the battlefield")
    void triggerResolvesAfterRosaLeavesBattlefield() {
        Permanent rosa = addCreatureReady(player1, new RosaResoluteWhiteMage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rosa);
        gd.playerGraveyards.get(player1.getId()).add(rosa.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("A target that leaves before resolution receives neither a counter nor lifelink")
    void removedTargetReceivesNeitherEffect() {
        Permanent rosa = addCreatureReady(player1, new RosaResoluteWhiteMage());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, rosa, Keyword.LIFELINK)).isFalse();
        assertThat(rosa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
