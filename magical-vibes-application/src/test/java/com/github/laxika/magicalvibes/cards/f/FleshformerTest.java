package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleshformerTest extends BaseCardTest {

    private void payFullCost() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("Boosts self +2/+2, grants fear, and gives target creature -2/-2")
    void boostsSelfGrantsFearAndDebuffsTarget() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        harness.addToBattlefield(player2, new HillGiant());
        Permanent hillGiant = findPermanent(player2, "Hill Giant");
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, hillGiant.getId());
        harness.passBothPriorities();

        assertThat(fleshformer.getEffectivePower()).isEqualTo(4);
        assertThat(fleshformer.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, fleshformer, Keyword.FEAR)).isTrue();
        assertThat(hillGiant.getEffectivePower()).isEqualTo(1);
        assertThat(hillGiant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-2/-2 kills a target with 2 toughness")
    void debuffKillsSmallTarget() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("All effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        harness.addToBattlefield(player2, new HillGiant());
        Permanent hillGiant = findPermanent(player2, "Hill Giant");
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, hillGiant.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fleshformer.getEffectivePower()).isEqualTo(2);
        assertThat(fleshformer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, fleshformer, Keyword.FEAR)).isFalse();
        assertThat(hillGiant.getEffectivePower()).isEqualTo(3);
        assertThat(hillGiant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        harness.addToBattlefield(player2, new HillGiant());
        Permanent hillGiant = findPermanent(player2, "Hill Giant");
        payFullCost();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(fleshformer), null, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        harness.addToBattlefield(player2, new HillGiant()); // legal creature target so the ability is activatable
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent fountain = findPermanent(player2, "Fountain of Youth");
        payFullCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(fleshformer), null, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
