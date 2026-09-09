package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SuperSkrull.class, FountainOfYouth.class, GrizzlyBears.class})
class SuperSkrullTest extends BaseCardTest {

    @Test
    @DisplayName("White ability creates a 0/4 Wall token with defender")
    void createsWallToken() {
        addReadySuperSkrull(player1);
        addMana(player1, ManaColor.WHITE, 1);
        addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().getColor()).isNull();
        assertThat(wall.getCard().getSubtypes()).containsExactly(CardSubtype.WALL);
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Green ability gives Super-Skrull +4/+4 until end of turn")
    void boostsSelfUntilEndOfTurn() {
        Permanent superSkrull = addReadySuperSkrull(player1);
        addMana(player1, ManaColor.GREEN, 1);
        addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(superSkrull.getPowerModifier()).isEqualTo(4);
        assertThat(superSkrull.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(superSkrull.getPowerModifier()).isZero();
        assertThat(superSkrull.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Red ability deals 4 damage to a target creature")
    void dealsDamageToTargetCreature() {
        addReadySuperSkrull(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addMana(player1, ManaColor.RED, 1);
        addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Red ability cannot target a noncreature permanent")
    void damageAbilityRequiresCreatureTarget() {
        addReadySuperSkrull(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        addMana(player1, ManaColor.RED, 1);
        addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Blue ability makes the target player draw four cards")
    void targetPlayerDrawsFourCards() {
        addReadySuperSkrull(player1);
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        addMana(player1, ManaColor.BLUE, 1);
        addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 4);
    }

    private Permanent addReadySuperSkrull(Player player) {
        return addCreatureReady(player, new SuperSkrull());
    }

    private void addMana(Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }
}
