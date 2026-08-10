package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        harness.assertInGraveyard(player1, "Lifespark Spellbomb");
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    @DisplayName("Colorless ability sacrifices the Spellbomb and draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lifespark Spellbomb");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Green ability cannot target a nonland permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new LifesparkSpellbomb());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");

        harness.assertOnBattlefield(player1, "Lifespark Spellbomb");
    }

    private Permanent addLand() {
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }
}
