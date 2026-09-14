package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelForge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoonOfBoseiju.class, DarksteelForge.class, GrizzlyBears.class, Pacifism.class})
class BoonOfBoseijuTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target by the greatest controlled permanent mana value and untaps it")
    void boostsByGreatestControlledPermanentManaValueAndUntaps() {
        Permanent target = addTappedCreature(player1);
        harness.addToBattlefield(player1, new DarksteelForge());

        cast(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(11);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ignores permanents controlled by opponents")
    void ignoresOpponentsPermanents() {
        Permanent target = addTappedCreature(player1);
        harness.addToBattlefield(player2, new DarksteelForge());

        cast(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addTappedCreature(player1);
        harness.addToBattlefield(player1, new DarksteelForge());

        cast(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        harness.setHand(player1, List.of(new BoonOfBoseiju()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new BoonOfBoseiju()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent target = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        target.tap();
        return target;
    }
}
