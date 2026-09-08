package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightshieldArray;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        GrizzlyBears.class,
        InvasionOfGobakhan.class,
        LightshieldArray.class,
        Plains.class,
        Shock.class
})
class InvasionOfGobakhanTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at an opponent's hand and may exile a nonland card with its owner's permission and a tax")
    void looksAtHandAndExilesNonlandWithOwnerPermissionAndTax() {
        Card shock = new Shock();
        Card plains = new Plains();
        harness.setHand(player2, List.of(shock, plains));

        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        assertThat(choice.optional()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(shock);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(plains);
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player2.getId());
        ExilePlayCostModifier modifier = gd.exilePlayCostModifiers.get(shock.getId());
        assertThat(modifier.permittedPlayerId()).isEqualTo(player2.getId());
        assertThat(modifier.sourceControllerId()).isEqualTo(player1.getId());
        assertThat(modifier.amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Lightshield Array transformed")
    void defeatCastsBackFace() {
        harness.setHand(player2, List.of(new Forest()));
        castInvasion();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battle = findPermanent(player1, "Invasion of Gobakhan");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent backFace = findPermanent(player1, "Lightshield Array");
        assertThat(backFace.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Lightshield Array puts counters on creatures that attacked this turn at end step")
    void endStepCountersAttackers() {
        harness.addToBattlefield(player1, new LightshieldArray());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Lightshield Array protects creatures you control until end of turn")
    void sacrificeGrantsHexproofAndIndestructible() {
        Permanent array = harness.addToBattlefieldAndReturn(player1, new LightshieldArray());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(array);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private void castInvasion() {
        harness.setHand(player1, List.of(new InvasionOfGobakhan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
