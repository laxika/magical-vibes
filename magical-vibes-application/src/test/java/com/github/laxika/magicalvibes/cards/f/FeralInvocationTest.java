package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.v.VoyagingSatyr;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeralInvocation.class, VoyagingSatyr.class, Forest.class})
class FeralInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Feral Invocation can be cast during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        Permanent satyr = addCreatureReady(player2, new VoyagingSatyr());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new FeralInvocation()));
        addMana();
        gs.passPriority(gd, player2);

        harness.castEnchantment(player1, 0, satyr.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Feral Invocation attaches it and gives the creature +2/+2")
    void resolvingAttachesAndBoosts() {
        Permanent satyr = addCreatureReady(player1, new VoyagingSatyr());
        harness.setHand(player1, List.of(new FeralInvocation()));
        addMana();

        harness.castEnchantment(player1, 0, satyr.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Feral Invocation");
        assertThat(aura.getAttachedTo()).isEqualTo(satyr.getId());
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost ends when Feral Invocation leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent satyr = addCreatureReady(player1, new VoyagingSatyr());
        Permanent aura = new Permanent(new FeralInvocation());
        aura.setAttachedTo(satyr.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Feral Invocation cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FeralInvocation()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Feral Invocation boosts an opponent's creature without changing its controller")
    void boostsOpponentsCreature() {
        Permanent satyr = addCreatureReady(player2, new VoyagingSatyr());
        Permanent other = addCreatureReady(player2, new VoyagingSatyr());
        harness.setHand(player1, List.of(new FeralInvocation()));
        addMana();

        harness.castEnchantment(player1, 0, satyr.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Feral Invocation").getAttachedTo()).isEqualTo(satyr.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(satyr);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(satyr);
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Feral Invocation goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent satyr = addCreatureReady(player1, new VoyagingSatyr());
        harness.setHand(player1, List.of(new FeralInvocation()));
        addMana();
        harness.castEnchantment(player1, 0, satyr.getId());

        gd.playerBattlefields.get(player1.getId()).remove(satyr);
        harness.setGraveyard(player1, List.of(satyr.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Feral Invocation");
        harness.assertInGraveyard(player1, "Feral Invocation");
    }

    @Test
    @DisplayName("Multiple Feral Invocations on the same creature have cumulative boosts")
    void multipleAurasStack() {
        Permanent satyr = addCreatureReady(player1, new VoyagingSatyr());
        harness.setHand(player1, List.of(new FeralInvocation(), new FeralInvocation()));
        addMana();
        addMana();

        harness.castEnchantment(player1, 0, satyr.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, satyr.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Feral Invocation"))
                .hasSize(2)
                .allSatisfy(aura -> assertThat(aura.getAttachedTo()).isEqualTo(satyr.getId()));
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(6);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
