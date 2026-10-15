package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({InfinityFormula.class, GrizzlyBears.class})
class InfinityFormulaTest extends BaseCardTest {

    @Test
    @DisplayName("Infinity Formula enters attached to a target creature you control")
    void entersAttachedToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InfinityFormula()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent formula = findPermanent(player1, "Infinity Formula");
        assertThat(formula.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature gains 2 life when it attacks")
    void equippedCreatureAttackGainsLife() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent formula = harness.addToBattlefieldAndReturn(player1, new InfinityFormula());
        formula.setAttachedTo(bears.getId());
        harness.setLife(player1, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Equip {2} attaches Infinity Formula to another creature you control")
    void equipAttachesToAnotherCreature() {
        Permanent formula = harness.addToBattlefieldAndReturn(player1, new InfinityFormula());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(formula.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void movingEquipmentRemovesBoostAndAttackAbilityFromPreviousCreature() {
        Permanent formula = harness.addToBattlefieldAndReturn(player1, new InfinityFormula());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        formula.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(formula.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first)));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void grantedAttackAbilityBenefitsCreatureControllerRatherThanEquipmentController() {
        Permanent formula = harness.addToBattlefieldAndReturn(player1, new InfinityFormula());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        formula.setAttachedTo(bears.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(bears)));
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(22);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void canEnterWithoutAnyCreatureToAttachTo() {
        harness.setHand(player1, List.of(new InfinityFormula()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Infinity Formula").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
