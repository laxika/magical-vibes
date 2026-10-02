package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishHandservant;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CennsHeir.class, GoldmeadowStalwart.class, ElvishHandservant.class, RayOfCommand.class})
class CennsHeirTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getId().equals(heir.getCard().getId()));
    }

    @Test
    @DisplayName("Gets +0/+0 when attacking alone (no other Kithkin)")
    void noBoostWhenAttackingAlone() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+1 when attacking with one other Kithkin")
    void boostWithOneOtherKithkin() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, new GoldmeadowStalwart());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);
        assertThat(heir.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 when attacking with two other Kithkin")
    void boostWithTwoOtherKithkin() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, new GoldmeadowStalwart());
        addCreatureReady(player1, new GoldmeadowStalwart());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(2);
        assertThat(heir.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Kithkin attackers do not count")
    void nonKithkinAttackersDoNotCount() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, new ElvishHandservant());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only counts attacking Kithkin, not non-attacking ones")
    void onlyCountsAttackingKithkin() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, new GoldmeadowStalwart());
        addCreatureReady(player1, new GoldmeadowStalwart());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);
        assertThat(heir.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        addCreatureReady(player1, new GoldmeadowStalwart());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(heir.getPowerModifier()).isEqualTo(0);
        assertThat(heir.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts an attacking Kithkin after an opponent gains control of it")
    void countsAttackingKithkinControlledByOpponent() {
        Permanent heir = addCreatureReady(player1, new CennsHeir());
        Permanent attackingKithkin = addCreatureReady(player1, new GoldmeadowStalwart());

        declareAttackers(player1, List.of(0, 1));

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, attackingKithkin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attackingKithkin.getId()));

        resolveAllTriggers();

        assertThat(heir.getPowerModifier()).isEqualTo(1);
        assertThat(heir.getToughnessModifier()).isEqualTo(1);
    }
}
