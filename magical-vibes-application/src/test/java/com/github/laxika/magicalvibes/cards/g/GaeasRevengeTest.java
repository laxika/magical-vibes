package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Snakeform;
import com.github.laxika.magicalvibes.cards.t.Triskelion;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaeasRevenge.class, Cancel.class, LightningBolt.class, GiantGrowth.class,
        ProdigalPyromancer.class, Triskelion.class, PaintersServant.class, Snakeform.class})
class GaeasRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Gaea's Revenge cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        GaeasRevenge gaeas = new GaeasRevenge();
        harness.setHand(player1, List.of(gaeas));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, gaeas.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gaea's Revenge");
        harness.assertNotInGraveyard(player1, "Gaea's Revenge");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Opponent cannot target Gaea's Revenge with a red spell")
    void opponentCannotTargetWithNongreenSpell() {
        Permanent gaeasPerm = addCreatureReady(player1, new GaeasRevenge());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, gaeasPerm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller also cannot target Gaea's Revenge with a nongreen spell")
    void controllerCannotTargetWithNongreenSpell() {
        Permanent gaeasPerm = addCreatureReady(player1, new GaeasRevenge());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, gaeasPerm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green spell can target Gaea's Revenge")
    void greenSpellCanTarget() {
        Permanent gaeasPerm = addCreatureReady(player1, new GaeasRevenge());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, gaeasPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Opponent's green spell can also target Gaea's Revenge")
    void opponentGreenSpellCanTarget() {
        Permanent gaeasPerm = addCreatureReady(player1, new GaeasRevenge());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, gaeasPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Abilities from nongreen sources cannot target Gaea's Revenge")
    void abilitiesFromNongreenSourceCannotTarget() {
        Permanent gaeasPerm = addCreatureReady(player2, new GaeasRevenge());

        // ProdigalPyromancer is red, its ability should not be able to target Gaea's Revenge
        addCreatureReady(player1, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gaeasPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target");
    }

    @Test
    @DisplayName("Can attack on the turn it enters because of haste")
    void canAttackImmediately() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        Permanent gaeas = harness.addToBattlefieldAndReturn(player1, new GaeasRevenge());
        gaeas.setSummoningSick(true);

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Colorless sources cannot target Gaea's Revenge")
    void colorlessAbilityCannotTarget() {
        Permanent gaeas = addCreatureReady(player2, new GaeasRevenge());
        Permanent triskelion = harness.addToBattlefieldAndReturn(player1, new Triskelion());
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gaeas.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target");
    }

    @Test
    @DisplayName("An ability from a red source that has gained green can target Gaea's Revenge")
    void abilityFromSourceMadeGreenCanTarget() {
        Permanent gaeas = addCreatureReady(player2, new GaeasRevenge());
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent painter = harness.addToBattlefieldAndReturn(player1, new PaintersServant());
        painter.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, null, gaeas.getId());
        harness.passBothPriorities();

        assertThat(gaeas.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless source that has gained green can target Gaea's Revenge")
    void abilityFromColorlessSourceMadeGreenCanTarget() {
        Permanent gaeas = addCreatureReady(player2, new GaeasRevenge());
        Permanent triskelion = harness.addToBattlefieldAndReturn(player1, new Triskelion());
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent painter = harness.addToBattlefieldAndReturn(player1, new PaintersServant());
        painter.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, null, gaeas.getId());
        harness.passBothPriorities();

        assertThat(gaeas.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Losing all abilities removes the restriction on nongreen targeting")
    void losingAbilitiesAllowsNongreenTargeting() {
        Permanent gaeas = addCreatureReady(player2, new GaeasRevenge());
        harness.setHand(player1, List.of(new Snakeform(), new LightningBolt()));
        harness.setLibrary(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, gaeas.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, gaeas.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gaea's Revenge");
    }
}
