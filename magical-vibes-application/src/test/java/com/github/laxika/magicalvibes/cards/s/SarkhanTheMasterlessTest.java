package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhanTheMasterless.class, NarsetParterOfVeils.class, GrizzlyBears.class})
class SarkhanTheMasterlessTest extends BaseCardTest {

    @Test
    @DisplayName("+1 animates each planeswalker you control as a red Dragon with flying")
    void plusOneAnimatesControlledPlaneswalkers() {
        Permanent sarkhan = addReadySarkhan(player1, 5);
        Permanent narset = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertAnimatedDragon(sarkhan);
        assertAnimatedDragon(narset);
    }

    @Test
    @DisplayName("-3 creates a 4/4 red Dragon token with flying")
    void minusThreeCreatesDragon() {
        Permanent sarkhan = addReadySarkhan(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger deals one damage for each Dragon to the attacking creature")
    void dragonsDamageAttacker() {
        addReadySarkhan(player1, 7);
        createDragonToken();
        createDragonToken();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0), null);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger also fires when a creature attacks a planeswalker")
    void attackOnPlaneswalkerTriggersAbility() {
        addReadySarkhan(player1, 5);
        Permanent narset = harness.addToBattlefieldAndReturn(player1, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 3);
        createDragonToken();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0), Map.of(0, narset.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    private void assertAnimatedDragon(Permanent planeswalker) {
        assertThat(gqs.isCreature(gd, planeswalker)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, planeswalker)).isFalse();
        assertThat(gqs.getEffectivePower(gd, planeswalker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, planeswalker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, planeswalker, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, planeswalker)).containsExactly(CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, planeswalker)).contains(CardSubtype.DRAGON);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, loyalty);
        sarkhan.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sarkhan;
    }

    private void createDragonToken() {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }
}
