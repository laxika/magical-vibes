package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AraAHeartOfTheSpider.class, GrizzlyBears.class})
class AraAHeartOfTheSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on a target attacking creature")
    void putsCounterOnTargetAttackingCreature() {
        addCreatureReady(player1, new AraAHeartOfTheSpider());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());
        harness.handlePermanentChosen(player1, secondAttacker.getId());
        resolveAllTriggers();

        assertThat(firstAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Modified creatures dealing combat damage exile the top card for this turn")
    void modifiedCreatureCombatDamageExilesTopCard() {
        addCreatureReady(player1, new AraAHeartOfTheSpider());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedAttacker.setAttacking(true);
        unmodifiedAttacker.setAttacking(true);

        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Each modified creature creates its own exile trigger")
    void eachModifiedCreatureExilesOneCard() {
        addCreatureReady(player1, new AraAHeartOfTheSpider());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        first.setAttacking(true);
        second.setAttacking(true);
        GrizzlyBears firstCard = new GrizzlyBears();
        GrizzlyBears secondCard = new GrizzlyBears();
        GrizzlyBears remainingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard, remainingCard));

        resolveCombatTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(firstCard.getId(), secondCard.getId());
    }

    @Test
    @DisplayName("Unmodified creatures do not trigger the exile ability")
    void unmodifiedCreatureDoesNotExile() {
        addCreatureReady(player1, new AraAHeartOfTheSpider());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Counters other than +1/+1 counters also make a creature modified")
    void stunCounterMakesCreatureModified() {
        Permanent attacker = addCreatureReady(player1, new AraAHeartOfTheSpider());
        attacker.setCounterCount(CounterType.STUN, 1);
        attacker.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The combat damage ability safely resolves with an empty library")
    void emptyLibraryDoesNotExile() {
        Permanent attacker = addCreatureReady(player1, new AraAHeartOfTheSpider());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of());

        resolveCombatTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveCombatTrigger() {
        resolveCombat(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
