package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BogardanHellkite;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdricMathematicalGenius.class, ProdigalPyromancer.class, Shock.class,
        AngelOfMercy.class, BogardanHellkite.class})
class AdricMathematicalGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an activated ability you control")
    void copiesActivatedAbilityYouControl() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AdricMathematicalGenius());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();
        harness.activateAbility(player1, 0, null, pyromancerAbilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices itself to counter an activated or triggered ability")
    void sacrificesItselfToCounterActivatedAbility() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new AdricMathematicalGenius());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        UUID pyromancerAbilityId = gd.stack.getLast().getCard().getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, pyromancerAbilityId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Adric, Mathematical Genius");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ultimate Sacrifice cannot target a spell")
    void ultimateSacrificeCannotTargetSpell() {
        addCreatureReady(player1, new AdricMathematicalGenius());
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, gd.stack.getLast().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseNewTargetForActivatedAbilityCopy() {
        addCreatureReady(player1, new AdricMathematicalGenius());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID target = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void cannotCopyOpponentsActivatedAbility() {
        addCreatureReady(player1, new AdricMathematicalGenius());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        UUID target = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copiesUntargetedTriggeredAbility() {
        addCreatureReady(player1, new AdricMathematicalGenius());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID target = gd.stack.getLast().getTargetableId();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target);
        resolveAllTriggers();

        harness.assertLife(player1, 26);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificesSummoningSickTappedAdricToCounterTriggeredAbility() {
        Permanent adric = harness.addToBattlefieldAndReturn(player1, new AdricMathematicalGenius());
        adric.tap();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID target = gd.stack.getLast().getTargetableId();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, target);
        harness.assertInGraveyard(player1, "Adric, Mathematical Genius");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Angel of Mercy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void offersNewTargetsForCopiedDividedDamageTrigger() {
        addCreatureReady(player1, new AdricMathematicalGenius());
        harness.setHand(player1, List.of(new BogardanHellkite()));
        harness.addMana(player1, ManaColor.RED, 8);
        gd.pendingETBDamageAssignments = Map.of(player1.getId(), 2, player2.getId(), 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID target = gd.stack.getLast().getTargetableId();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void copyFailsWhenOriginalAbilityIsCounteredBeforeResolution() {
        addCreatureReady(player1, new AdricMathematicalGenius());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID target = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, null, target);
        harness.activateAbility(player1, 0, 1, null, target);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Adric, Mathematical Genius");
        harness.assertOnBattlefield(player1, "Prodigal Pyromancer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickAdricCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new AdricMathematicalGenius());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID target = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyAbilityCannotTargetSpellYouControl() {
        addCreatureReady(player1, new AdricMathematicalGenius());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        UUID target = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
