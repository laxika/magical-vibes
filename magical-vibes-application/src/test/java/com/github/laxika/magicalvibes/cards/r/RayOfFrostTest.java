package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RayOfFrost.class, FountainOfYouth.class, GrizzlyBears.class, ProdigalPyromancer.class})
class RayOfFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Ray of Frost taps a red creature when it enters")
    void tapsRedCreatureOnEnter() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        castRayOfFrost(pyromancer);

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ray of Frost does not tap a nonred creature when it enters")
    void doesNotTapNonredCreatureOnEnter() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castRayOfFrost(bears);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ray of Frost removes a red creature's activated abilities")
    void redCreatureLosesAbilities() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent aura = new Permanent(new RayOfFrost());
        aura.setAttachedTo(pyromancer.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ray of Frost keeps the enchanted creature tapped through its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        pyromancer.tap();
        attachRayOfFrost(pyromancer);

        advanceToNextTurn(player1);

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ray of Frost cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RayOfFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castRayOfFrost(Permanent target) {
        harness.setHand(player1, List.of(new RayOfFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void attachRayOfFrost(Permanent target) {
        Permanent aura = new Permanent(new RayOfFrost());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
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
