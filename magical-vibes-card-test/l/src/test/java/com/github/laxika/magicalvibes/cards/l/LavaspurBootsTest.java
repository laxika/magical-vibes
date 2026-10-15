package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({LavaspurBoots.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class})
class LavaspurBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and haste")
    void equippedCreatureGetsBoostAndHaste() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equip ability attaches the Boots to a creature")
    void equipAttachesBoots() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when they pay")
    void wardAllowsPaidSpell() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability without removing its source")
    void wardCountersUnpaidActivatedAbility() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pyromancer);
        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying ward allows an opponent's activated ability to resolve")
    void wardAllowsPaidActivatedAbility() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, bears.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's own spell does not trigger ward")
    void ownSpellDoesNotTriggerWard() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("An opponent may decline ward even when they have enough mana")
    void wardCountersSpellWhenPaymentDeclined() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Reequipping moves the boost and haste and removes ward from the old creature")
    void reequippingMovesGrantedAbilities() {
        Permanent boots = addReadyBoots(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        boots.setAttachedTo(first.getId());
        assertThat(als.canAttack(gd, first, player1.getId())).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(als.canAttack(gd, first, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, second, player1.getId())).isTrue();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, first.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second).doesNotContain(first);
    }

    private Permanent addReadyBoots(Player player) {
        Permanent boots = harness.addToBattlefieldAndReturn(player, new LavaspurBoots());
        boots.setSummoningSick(false);
        return boots;
    }
}
