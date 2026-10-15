package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BalefulEidolon.class, TravelingPhilosopher.class})
class BalefulEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Baleful Eidolon can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new BalefulEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
    }

    @Test
    @DisplayName("Baleful Eidolon can be bestowed to boost a creature and grant it deathtouch")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BalefulEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Baleful Eidolon becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BalefulEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature when the target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BalefulEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Baleful Eidolon");
        harness.assertNotInGraveyard(player1, "Baleful Eidolon");
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature and only boosts its host")
    void bestowsOnOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new BalefulEidolon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(eidolon.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, eidolon)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, host, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Baleful Eidolon's deathtouch kills a creature with greater toughness")
    void deathtouchKillsLargerCreature() {
        Permanent eidolon = addCreatureReady(player2, new BalefulEidolon());
        Permanent attacker = addCreatureReady(player1, new TravelingPhilosopher());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(eidolon);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Traveling Philosopher");
        harness.assertInGraveyard(player1, "Traveling Philosopher");
        harness.assertNotOnBattlefield(player2, "Baleful Eidolon");
        harness.assertInGraveyard(player2, "Baleful Eidolon");
    }
}
