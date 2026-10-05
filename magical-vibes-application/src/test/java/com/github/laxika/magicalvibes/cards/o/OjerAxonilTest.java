package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TempleOfPower;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjerAxonil.class, TempleOfPower.class, Shock.class, Murder.class, DressDown.class})
class OjerAxonilTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces small red noncombat damage dealt to an opponent with four damage")
    void replacesSmallRedNoncombatDamageToOpponent() {
        harness.addToBattlefield(player1, new OjerAxonil());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Returns tapped and transformed under its owner's control when it dies")
    void returnsTappedAndTransformedWhenItDies() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerAxonil());
        destroyOjer(ojer);

        Permanent temple = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TempleOfPower)
                .findFirst()
                .orElseThrow();
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforms after red sources deal four noncombat damage this turn")
    void transformsAfterThresholdIsMet() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.redSourceNoncombatDamageThisTurn.get(player1.getId())).isEqualTo(4);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null);
        harness.passBothPriorities();

        assertThat(temple.getCard()).isInstanceOf(OjerAxonil.class);
        assertThat(temple.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot transform before red sources deal four noncombat damage")
    void cannotTransformBeforeThresholdIsMet() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);
        assertThat(temple.getCard()).isInstanceOf(TempleOfPower.class);
    }

    @Test
    void usesPowerAtDamageResolution() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerAxonil());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        ojer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    void doesNotIncreaseDamageToController() {
        harness.addToBattlefield(player1, new OjerAxonil());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotIncreaseOpponentsRedDamage() {
        harness.addToBattlefield(player1, new OjerAxonil());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotIncreaseDamageToCreatures() {
        harness.addToBattlefield(player1, new OjerAxonil());
        Permanent opponentOjer = harness.addToBattlefieldAndReturn(player2, new OjerAxonil());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, opponentOjer.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentOjer);
        assertThat(opponentOjer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotReplaceDamageAfterLosingAbilities() {
        harness.addToBattlefield(player1, new OjerAxonil());
        harness.addToBattlefield(player2, new DressDown());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void templeAddsRedManaWithoutDamageThreshold() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(temple), 0, null, null);

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
    }

    @Test
    void templeCannotTransformOutsideMainPhaseEvenWithEnoughDamage() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);
        assertThat(temple.isTapped()).isFalse();
        assertThat(temple.getCard()).isInstanceOf(TempleOfPower.class);
    }

    @Test
    void templeCountsDamageDealtToItsController() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null);
        harness.passBothPriorities();

        assertThat(temple.getCard()).isInstanceOf(OjerAxonil.class);
        assertThat(temple.isTapped()).isTrue();
        harness.assertLife(player1, 16);
    }

    private void destroyOjer(Permanent ojer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, ojer.getId());
        harness.passBothPriorities();
    }

    private Permanent returnOjerAsTemple() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerAxonil());
        destroyOjer(ojer);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TempleOfPower)
                .findFirst()
                .orElseThrow();
    }
}
