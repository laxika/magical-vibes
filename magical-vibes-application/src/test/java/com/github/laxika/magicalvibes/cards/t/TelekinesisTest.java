package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.ScrybSprites;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Telekinesis.class, ScrybSprites.class, Forest.class, LightningBolt.class})
class TelekinesisTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target, prevents its combat damage, and skips its next two untap steps")
    void appliesAllEffects() {
        Permanent target = addCreatureReady(player2, new ScrybSprites());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(2);

        target.untap();
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        harness.setLife(player1, 20);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents combat damage only from the targeted creature")
    void onlyPreventsTargetCombatDamage() {
        Permanent target = addCreatureReady(player2, new ScrybSprites());
        Permanent otherAttacker = addCreatureReady(player2, new ScrybSprites());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        target.untap();
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        otherAttacker.setAttacking(true);
        otherAttacker.setAttackTarget(player1.getId());
        harness.setLife(player1, 20);

        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage dealt by another source")
    void doesNotPreventNoncombatDamage() {
        Permanent target = addCreatureReady(player2, new ScrybSprites());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scryb Sprites");
    }

    @Test
    @DisplayName("Keeps the target tapped through its controller's next two untap steps")
    void skipsNextTwoUntapSteps() {
        Permanent target = addCreatureReady(player2, new ScrybSprites());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);

        advanceToUpkeep(player1);
        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();

        advanceToUpkeep(player1);
        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Telekinesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
