package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaziaBorosArchangel.class, GrizzlyBears.class, ProdigalPyromancer.class, LightningBolt.class,
        ImprisonedInTheMoon.class, SiegeWurm.class, ViashinoFangtail.class})
class RaziaBorosArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next three damage from a controlled creature to another creature")
    void redirectsNextThreeDamage() {
        Permanent razia = addCreatureReady(player1, new RaziaBorosArchangel());
        Permanent protectedCreature = addReadyStats(player1, 4, 4);
        Permanent destination = addReadyStats(player2, 5, 5);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbilityWithMultiTargets(player1, indexOf(player1, razia), 0,
                List.of(protectedCreature.getId(), destination.getId()));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, protectedCreature.getId());

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(3);

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not redirect damage when a target stops being a creature before resolution")
    void ignoresTargetThatIsNoLongerACreatureOnResolution() {
        Permanent razia = addCreatureReady(player1, new RaziaBorosArchangel());
        Permanent protectedCreature = addReadyStats(player1, 4, 4);
        Permanent destination = addReadyStats(player2, 5, 5);

        harness.activateAbilityWithMultiTargets(player1, indexOf(player1, razia), 0,
                List.of(protectedCreature.getId(), destination.getId()));

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(protectedCreature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires the protected and destination creatures to be different")
    void requiresDifferentTargets() {
        Permanent razia = addCreatureReady(player1, new RaziaBorosArchangel());
        Permanent creature = addReadyStats(player1, 4, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, indexOf(player1, razia), 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Redirected damage can be redirected again by another Razia")
    void appliesAnotherRedirectionToRedirectedDamage() {
        Permanent firstRazia = addCreatureReady(player1, new RaziaBorosArchangel());
        Permanent secondRazia = addCreatureReady(player2, new RaziaBorosArchangel());
        Permanent first = addCreatureReady(player1, new SiegeWurm());
        Permanent second = addCreatureReady(player2, new SiegeWurm());
        Permanent third = addCreatureReady(player1, new SiegeWurm());
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());

        harness.activateAbilityWithMultiTargets(player1, indexOf(player1, firstRazia), 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithMultiTargets(player2, indexOf(player2, secondRazia), 0,
                List.of(second.getId(), third.getId()));
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, fangtail), null, first.getId());
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(third.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not redirect damage to a destination that becomes only a land")
    void doesNotRedirectToLandAfterResolution() {
        Permanent razia = addCreatureReady(player1, new RaziaBorosArchangel());
        Permanent protectedCreature = addCreatureReady(player1, new SiegeWurm());
        Permanent destination = addCreatureReady(player2, new SiegeWurm());
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());

        harness.activateAbilityWithMultiTargets(player1, indexOf(player1, razia), 0,
                List.of(protectedCreature.getId(), destination.getId()));
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new ImprisonedInTheMoon()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, destination.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, fangtail), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The three-damage limit is shared across separate damage events")
    void consumesShieldAcrossSeparateEvents() {
        Permanent razia = addCreatureReady(player1, new RaziaBorosArchangel());
        Permanent protectedCreature = addCreatureReady(player1, new SiegeWurm());
        Permanent destination = addCreatureReady(player1, new SiegeWurm());
        List<Permanent> fangtails = List.of(
                addCreatureReady(player1, new ViashinoFangtail()),
                addCreatureReady(player1, new ViashinoFangtail()),
                addCreatureReady(player1, new ViashinoFangtail()),
                addCreatureReady(player1, new ViashinoFangtail()));

        harness.activateAbilityWithMultiTargets(player1, indexOf(player1, razia), 0,
                List.of(protectedCreature.getId(), destination.getId()));
        harness.passBothPriorities();
        for (int i = 0; i < fangtails.size(); i++) {
            harness.activateAbility(player1, indexOf(player1, fangtails.get(i)), null,
                    protectedCreature.getId());
            harness.passBothPriorities();
            assertThat(destination.getMarkedDamage()).isEqualTo(Math.min(i + 1, 3));
            assertThat(protectedCreature.getMarkedDamage()).isEqualTo(Math.max(i - 2, 0));
        }
    }

    @Test
    @DisplayName("The protected creature must be controlled by the ability's controller")
    void cannotProtectOpponentsCreature() {
        Permanent razia = addCreatureReady(player1, new RaziaBorosArchangel());
        Permanent opponentCreature = addCreatureReady(player2, new SiegeWurm());
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, indexOf(player1, razia), 0,
                List.of(opponentCreature.getId(), razia.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
