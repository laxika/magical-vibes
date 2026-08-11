package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SingingBellStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Singing Bell Strike taps and attaches to the target creature")
    void entersTappedAndAttached() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SingingBellStrike()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Singing Bell Strike")
                        && permanent.isAttached()
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Singing Bell Strike prevents the enchanted creature from untapping")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        Permanent aura = new Permanent(new SingingBellStrike());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature can pay {6} to untap itself")
    void enchantedCreatureCanUntapForSixMana() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        Permanent aura = new Permanent(new SingingBellStrike());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Singing Bell Strike cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SingingBellStrike()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent forest = findPermanent(player1, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
