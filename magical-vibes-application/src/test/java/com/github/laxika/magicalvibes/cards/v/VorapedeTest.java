package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VorapedeTest extends BaseCardTest {

    @Test
    @DisplayName("Has vigilance, trample, and undying keywords")
    void hasKeywords() {
        Permanent vorapede = addCreatureReady(player1, new Vorapede());

        assertThat(gqs.hasKeyword(gd, vorapede, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vorapede, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vorapede, Keyword.UNDYING)).isTrue();
    }

    @Test
    @DisplayName("Vigilance: Vorapede does not tap when attacking")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent vorapede = addCreatureReady(player1, new Vorapede());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Combat may auto-resolve and clear isAttacking; tapped state persists
        assertThat(vorapede.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent vorapede = addCreatureReady(player1, new Vorapede());
        vorapede.setAttacking(true);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 5/4 trample blocked by 2/2 → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                bears.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Undying returns Vorapede with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent vorapede = harness.addToBattlefieldAndReturn(player1, new Vorapede());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, vorapede.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedVorapede = findPermanent(player1, "Vorapede");
        assertThat(returnedVorapede.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedVorapede.getEffectivePower()).isEqualTo(6);
        assertThat(returnedVorapede.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Vorapede"));
    }

    @Test
    @DisplayName("Undying does not return Vorapede when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent vorapede = harness.addToBattlefieldAndReturn(player1, new Vorapede());
        vorapede.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, vorapede.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Vorapede"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Vorapede"));
        assertThat(gd.stack).isEmpty();
    }
}
