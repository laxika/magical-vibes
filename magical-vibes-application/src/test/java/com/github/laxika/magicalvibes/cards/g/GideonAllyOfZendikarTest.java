package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonAllyOfZendikar.class, GrizzlyBears.class, Shock.class, Skullcrack.class})
class GideonAllyOfZendikarTest extends BaseCardTest {

    @Test
    @DisplayName("+1 animates Gideon into a 5/5 indestructible Human Soldier Ally")
    void plusOneAnimatesGideon() {
        Permanent gideon = addReadyGideon(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, gideon))
                .contains(CardSubtype.HUMAN, CardSubtype.SOLDIER, CardSubtype.ALLY);
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 prevents damage to Gideon for the turn")
    void plusOnePreventsDamageToGideon() {
        Permanent gideon = addReadyGideon(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("0 creates a 2/2 white Knight Ally token")
    void zeroCreatesKnightAllyToken() {
        Permanent gideon = addReadyGideon(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Knight Ally");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KNIGHT, CardSubtype.ALLY);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-4 gives creatures you control +1/+1 through an emblem")
    void minusFourCreatesAnthemEmblem() {
        Permanent gideon = addReadyGideon(player1, 4);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unpreventable damage both marks animated Gideon and removes loyalty")
    void unpreventableDamageHasCreatureAndPlaneswalkerResults() {
        Permanent gideon = addReadyGideon(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Skullcrack(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gideon.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Gideon, Ally of Zendikar");
    }

    @Test
    @DisplayName("Animation, indestructible, and damage prevention expire at cleanup")
    void plusOneExpiresAtCleanup() {
        Permanent gideon = addReadyGideon(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, gideon)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, gideon)).isTrue();
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("The emblem resolves after Gideon dies and boosts future creatures")
    void emblemBoostsFutureCreaturesAfterGideonDies() {
        addReadyGideon(player1, 4);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertNotOnBattlefield(player1, "Gideon, Ally of Zendikar");
        harness.assertInGraveyard(player1, "Gideon, Ally of Zendikar");
        harness.passBothPriorities();

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }
    private Permanent addReadyGideon(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GideonAllyOfZendikar());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
