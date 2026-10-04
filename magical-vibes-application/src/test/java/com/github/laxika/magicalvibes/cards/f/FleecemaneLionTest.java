package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleecemaneLion.class, Murder.class, Shock.class})
class FleecemaneLionTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts a +1/+1 counter on Fleecemane Lion")
    void monstrosityAddsCounterAndMarksItMonstrous() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lion.isMonstrous()).isTrue();
        assertThat(lion.getEffectivePower()).isEqualTo(4);
        assertThat(lion.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Monstrous Fleecemane Lion has hexproof and indestructible")
    void monstrousAbilitiesApply() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        activateMonstrosity(lion);

        assertThat(gqs.hasKeyword(gd, lion, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, lion, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Monstrous Fleecemane Lion cannot be targeted by an opponent")
    void opponentCannotTargetMonstrousLion() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        activateMonstrosity(lion);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, lion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Monstrous Fleecemane Lion survives a destroy effect")
    void monstrousLionSurvivesDestroyEffect() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        activateMonstrosity(lion);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, lion.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleecemane Lion");
        harness.assertNotInGraveyard(player1, "Fleecemane Lion");
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing after becoming monstrous")
    void monstrosityCanBeActivatedAgainButOnlyAddsCountersOnce() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        activateMonstrosity(lion);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(lion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lion.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Fleecemane Lion gains its protections only when monstrosity resolves")
    void protectionsWaitForResolution() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(lion.isMonstrous()).isFalse();
        assertThat(lion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lion, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, lion, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(lion.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, lion, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, lion, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Two pending monstrosity activations add only one counter")
    void multiplePendingActivationsOnlyAddOneCounter() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(lion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(lion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lion.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Removing its counter does not remove monstrous or the protections")
    void removingCounterDoesNotRemoveMonstrousProtections() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        activateMonstrosity(lion);
        lion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(lion.isMonstrous()).isTrue();
        assertThat(gqs.hasKeyword(gd, lion, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, lion, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, lion.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleecemane Lion");
    }

    @Test
    @DisplayName("Becoming monstrous in response makes an opponent's destroy spell lose its target")
    void monstrosityInResponseProtectsLion() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, lion.getId());

        activateMonstrosity(lion);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(lion.isMonstrous()).isTrue();
        harness.assertOnBattlefield(player1, "Fleecemane Lion");
        harness.assertInGraveyard(player2, "Murder");
    }

    private void activateMonstrosity(Permanent lion) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(lion);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
