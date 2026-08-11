package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, lion.getId(), null))
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
    @DisplayName("Monstrosity cannot be activated again after it resolves")
    void monstrosityOnlyResolvesOnce() {
        Permanent lion = addCreatureReady(player1, new FleecemaneLion());
        activateMonstrosity(lion);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
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
