package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NinjaTeen.class, GrizzlyBears.class, Memnite.class, Unsummon.class})
class NinjaTeenTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when a creature you control leaves")
    void opponentLosesLifeWhenOwnCreatureLeaves() {
        harness.addToBattlefield(player1, new NinjaTeen());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int opponentLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    @DisplayName("Level 2 gives your creatures +1/+0 and menace")
    void levelTwoBoostsOwnCreaturesAndGrantsMenace() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new NinjaTeen());
        Permanent ownCreature = addCreatureReady(player1, new Memnite());
        Permanent opposingCreature = addCreatureReady(player2, new Memnite());

        levelUp(ninja, 0, 1);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Level 3 casts a creature from your graveyard with sneak")
    void levelThreeCastsCreatureWithSneak() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new NinjaTeen());
        levelUp(ninja, 0, 1);
        levelUp(ninja, 1, 0);

        Permanent attacker = addCreatureReady(player1, new Memnite());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        GrizzlyBears graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.getGameService().playFlashbackSpell(
                gd, player1, 0, null, null, List.of(), null, null, List.of(), null,
                attacker.getId(), List.of(), java.util.Map.of());
        resolveAllTriggers();

        Permanent sneaked = findPermanent(player1, "Grizzly Bears");
        assertThat(sneaked.isTapped()).isTrue();
        assertThat(sneaked.isAttacking()).isTrue();
        assertThat(sneaked.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
    }

    @Test
    @DisplayName("Level 3 requires an unblocked attacker for sneak")
    void levelThreeNeedsUnblockedAttacker() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new NinjaTeen());
        levelUp(ninja, 0, 1);
        levelUp(ninja, 1, 0);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void levelUp(Permanent ninja, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int ninjaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ninja);
        harness.activateAbility(player1, ninjaIndex, abilityIndex, null, null);
        resolveAllTriggers();
    }
}
