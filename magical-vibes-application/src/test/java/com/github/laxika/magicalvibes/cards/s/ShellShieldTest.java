package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.m.MightOfMurasa;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShellShield.class, CanopyBaloth.class, MightOfMurasa.class, IntoTheRoil.class})
class ShellShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, gives a creature +0/+3")
    void withoutKickerBoostsTarget() {
        Permanent target = addCreature(player1);

        castResolve(target, false);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("With kicker, also gives the creature hexproof")
    void withKickerGrantsHexproof() {
        Permanent target = addCreature(player1);

        castResolve(target, true);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();

        harness.setHand(player2, List.of(new MightOfMurasa()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and hexproof expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addCreature(player1);

        castResolve(target, true);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreature(player2);
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Hexproof still allows the controller to target the creature")
    void controllerCanTargetCreatureWithGrantedHexproof() {
        Permanent target = addCreature(player1);
        castResolve(target, true);

        harness.setHand(player1, List.of(new MightOfMurasa()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(6);
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Kicked Shell Shield makes an opponent's pending spell lose its target")
    void kickedShieldProtectsAgainstPendingSpell() {
        Permanent target = addCreature(player1);
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, target.getId());

        castResolve(target, true);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Baloth");
        harness.assertNotInHand(player1, "Canopy Baloth");
        harness.assertInGraveyard(player2, "Into the Roil");
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Kicked Shell Shield does not resolve if its target leaves in response")
    void removedTargetReceivesNeitherEffect() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castKickedInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Canopy Baloth");
        harness.assertInHand(player1, "Canopy Baloth");
        harness.assertInGraveyard(player1, "Shell Shield");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    private void castResolve(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, kicked ? 2 : 1);
        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
            harness.passBothPriorities();
        } else {
            harness.castAndResolveInstant(player1, 0, target.getId());
        }
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new CanopyBaloth());
        creature.setSummoningSick(false);
        return creature;
    }
}
