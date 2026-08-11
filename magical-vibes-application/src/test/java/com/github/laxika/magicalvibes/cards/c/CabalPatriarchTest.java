package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CabalPatriarchTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives a target creature -2/-2")
    void sacrificesCreatureForMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent sacrificialCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificialCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificialCreature);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling a creature card from the graveyard gives a target creature -2/-2")
    void exilesCreatureCardForMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears creatureCard = new GrizzlyBears();
        GiantGrowth noncreatureCard = new GiantGrowth();
        harness.setGraveyard(player1, List.of(creatureCard, noncreatureCard));
        addMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreatureCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void minusTwoMinusTwoWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a noncreature card to pay the second ability")
    void cannotExileNoncreatureCard() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GiantGrowth()));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
