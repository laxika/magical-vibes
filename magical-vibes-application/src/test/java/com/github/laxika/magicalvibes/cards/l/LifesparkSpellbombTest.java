package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LifesparkSpellbomb.class, Forest.class, GrizzlyBears.class})
class LifesparkSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("Green ability animates a target land into a 3/3 creature that is still a land")
    void animatesTargetLand() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        Permanent land = addLand();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.isLand(gd, land)).isTrue();
        harness.assertInGraveyard(player1, "Lifespark Spellbomb");
    }

    @Test
    @DisplayName("Green ability can animate a land an opponent controls")
    void animatesOpponentsLand() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Land animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        Permanent land = addLand();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Colorless ability sacrifices the Spellbomb and draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lifespark Spellbomb");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Green ability cannot target a nonland permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");

        harness.assertOnBattlefield(player1, "Lifespark Spellbomb");
    }

    @Test
    @DisplayName("Green ability does not animate a land that leaves before resolution")
    void doesNotAnimateTargetThatLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        Permanent land = addLand();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lifespark Spellbomb");
    }

    private Permanent addLand() {
        return harness.addToBattlefieldAndReturn(player1, new Forest());
    }
}
