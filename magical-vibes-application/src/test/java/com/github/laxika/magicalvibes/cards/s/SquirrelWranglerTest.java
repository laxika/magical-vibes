package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SquirrelWranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land creates two 1/1 green Squirrels")
    void sacrificeLandCreatesTwoSquirrels() {
        addReadyWrangler(player1);
        harness.addToBattlefield(player1, new Forest());
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("The second ability boosts every Squirrel until end of turn")
    void boostsAllSquirrelsUntilEndOfTurn() {
        addReadyWrangler(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent ownSquirrel = harness.addToBattlefieldAndReturn(player1, squirrelToken());
        Permanent opponentSquirrel = harness.addToBattlefieldAndReturn(player2, squirrelToken());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Neither ability can be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        addReadyWrangler(player1);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyWrangler(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new SquirrelWrangler());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Card squirrelToken() {
        Card card = new Card();
        card.setName("Squirrel");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SQUIRREL));
        card.setToken(true);
        return card;
    }
}
