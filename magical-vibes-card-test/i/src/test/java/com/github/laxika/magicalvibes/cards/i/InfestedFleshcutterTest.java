package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SinewDancer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfestedFleshcutter.class, GrizzlyBears.class, SinewDancer.class})
class InfestedFleshcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches the Equipment and gives the creature +2/+0")
    void equipBoostsCreature() {
        harness.addToBattlefield(player1, new InfestedFleshcutter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature creates a toxic Mite that can't block")
    void attackTriggerCreatesMite() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new InfestedFleshcutter());
        cutter.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent mite = findPermanent(player1, "Mite");
        assertThat(mite.getCard().isToken()).isTrue();
        assertThat(mite.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.MITE);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.TOXIC)).isTrue();
        assertThat(bls.canBlock(gd, mite)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger does not fire while the Equipment is unattached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new InfestedFleshcutter());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Infested Fleshcutter"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Mite"));
    }

    @Test
    @DisplayName("The created Mite applies toxic during combat damage without using the stack")
    void miteToxicAppliesWithCombatDamage() {
        Permanent creature = addCreatureReady(player1, new SinewDancer());
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new InfestedFleshcutter());
        cutter.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent mite = findPermanent(player1, "Mite");
        assertThat(mite.isAttacking()).isFalse();
        assertThat(mite.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, mite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mite)).isEqualTo(1);

        creature.setAttacking(false);
        mite.setSummoningSick(false);
        mite.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking with a different creature does not trigger the attached Equipment")
    void unrelatedAttackerDoesNotCreateMite() {
        Permanent equipped = addCreatureReady(player1, new SinewDancer());
        addCreatureReady(player1, new SinewDancer());
        Permanent cutter = harness.addToBattlefieldAndReturn(player1, new InfestedFleshcutter());
        cutter.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mite")).isZero();
    }
}
