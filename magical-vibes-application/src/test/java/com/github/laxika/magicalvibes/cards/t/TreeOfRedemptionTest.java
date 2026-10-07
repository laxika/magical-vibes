package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.r.RhoxFaithmender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TreeOfRedemption.class})
class TreeOfRedemptionTest extends BaseCardTest {


    @Test
    @DisplayName("Exchange sets life to toughness and toughness to old life total")
    void exchangeLifeAndToughness() {
        Permanent tree = addReadyTree(player1);
        // Default starting life is 20, Tree toughness is 13
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 13 (old toughness), toughness becomes 20 (old life)
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("Exchange when life is lower than toughness raises life")
    void exchangeWhenLifeLowerThanToughness() {
        Permanent tree = addReadyTree(player1);
        harness.setLife(player1, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 13, toughness becomes 5
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exchange when life equals toughness does nothing")
    void exchangeWhenLifeEqualsToughness() {
        Permanent tree = addReadyTree(player1);
        harness.setLife(player1, 13);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }

    @Test
    @DisplayName("Multiple exchanges: second exchange uses updated toughness")
    void multipleExchanges() {
        Permanent tree = addReadyTree(player1);
        // Life=20, Toughness=13

        // First exchange: life->13, toughness->20
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);

        // Untap for second activation
        tree.untap();

        // Second exchange: life->20, toughness->13
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }

    @Test
    @DisplayName("Toughness override persists across turns")
    void toughnessPersistsAcrossTurns() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Toughness is now 20
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);

        // Simulate turn reset (modifiers cleared, static recomputed)
        tree.resetModifiers();

        // Permanent base toughness override should survive
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("+1/+1 counters apply on top of exchanged toughness")
    void countersApplyOnTopOfExchangedToughness() {
        Permanent tree = addReadyTree(player1);
        tree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        // Effective toughness = 13 + 2 = 15

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 15 (effective toughness including counters)
        // New base toughness = 20 (old life), + 2 counters = 22
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(22);
    }


    @Test
    @DisplayName("Activating ability puts it on the stack")
    void putsAbilityOnStack() {
        addReadyTree(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tree of Redemption");
    }


    @Test
    @DisplayName("Activating ability taps Tree of Redemption")
    void activatingTapsTree() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(tree.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent tree = new Permanent(new TreeOfRedemption());
        tree.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(tree);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent tree = addReadyTree(player1);
        tree.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Equal life and effective toughness still sets base toughness")
    void equalValuesStillSetBaseToughness() {
        Permanent tree = addReadyTree(player1);
        tree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(17);
    }

    @Test
    @CardUsed({RhoxFaithmender.class})
    @DisplayName("Life gain from the exchange is doubled by Rhox Faithmender")
    void exchangeAppliesLifeGainReplacement() {
        Permanent tree = addReadyTree(player1);
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(5);
    }

    @Test
    @DisplayName("Exchange cannot lower life when life loss is prohibited")
    void exchangeDoesNotOccurWhenLifeLossIsProhibited() {
        Permanent tree = addReadyTree(player1);
        gd.playersWhoCantLoseLifeThisTurn.add(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }

    @Test
    @DisplayName("Exchange uses life and toughness at resolution")
    void exchangeUsesValuesAtResolution() {
        Permanent tree = addReadyTree(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.setLife(player1, 7);
        tree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(9);
    }

    @Test
    @DisplayName("Exchange has no effect if the Tree leaves before resolution")
    void exchangeDoesNotUseLastKnownToughness() {
        Permanent tree = addReadyTree(player1);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(tree);
        gd.playerGraveyards.get(player1.getId()).add(tree.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Tree of Redemption");
    }

    @Test
    @DisplayName("Exchange cannot raise life when life gain is prohibited")
    void exchangeDoesNotOccurWhenLifeGainIsProhibited() {
        Permanent tree = addReadyTree(player1);
        harness.setLife(player1, 5);
        gd.playersWhoCantGainLifeThisTurn.add(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 5);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }
    private Permanent addReadyTree(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TreeOfRedemption());
        perm.setSummoningSick(false);

        return perm;
    }
}
