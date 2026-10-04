package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({ElementalUprising.class, Forest.class, GrizzlyBears.class})
class ElementalUprisingTest extends BaseCardTest {

    @Test
    @DisplayName("Turns a land into a 4/4 Elemental with haste that must be blocked")
    void animatesLandAndRequiresItToBeBlocked() {
        Permanent land = addLand(player1);

        castOn(player1, land);

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(land.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Haste lets the animated land attack and the attack must be blocked")
    void animatedLandCanAttackAndMustBeBlocked() {
        Permanent land = addLand(player1);
        addCreatureReady(player2);

        castOn(player1, land);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Animation and must-be-blocked requirement expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent land = addLand(player1);

        castOn(player1, land);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isFalse();
        assertThat(land.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's land")
    void cannotTargetOpponentsLand() {
        Permanent opponentLand = addLand(player2);

        harness.setHand(player1, List.of(new ElementalUprising()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot evade the blocking requirement by blocking another attacker")
    void cannotRedirectOnlyBlockerToAnotherAttacker() {
        Permanent land = addLand(player1);
        addCreatureReady(player1);
        addCreatureReady(player2);
        castOn(player1, land);
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A single blocker satisfies the requirement even when more are available")
    void oneBlockerIsEnough() {
        Permanent land = addLand(player1);
        addCreatureReady(player2);
        addCreatureReady(player2);
        castOn(player1, land);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a creature that is not a land")
    void cannotTargetNonlandCreature() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new ElementalUprising()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void castOn(Player player, Permanent land) {
        harness.setHand(player, List.of(new ElementalUprising()));
        addManaForSpell();
        harness.castAndResolveInstant(player, 0, land.getId());
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
