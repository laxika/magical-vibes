package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeysaEnvoyOfGhosts.class, GrizzlyBears.class, LightningBolt.class, TajicBladeOfTheLegion.class})
class TeysaEnvoyOfGhostsTest extends BaseCardTest {

    @Test
    @DisplayName("Creature that deals combat damage to Teysa's controller is destroyed and a Spirit token is created")
    void combatDamageDestroysCreatureAndCreatesToken() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncombat damage to Teysa's controller does not trigger her ability")
    void noncombatDamageDoesNotTrigger() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Teysa has protection from creatures, so an attacking creature cannot damage her")
    void protectionFromCreaturesPreventsCombatDamage() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player2, new TeysaEnvoyOfGhosts());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(teysa.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Teysa, Envoy of Ghosts");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Each damaging creature generates a separate destruction and Spirit")
    void multipleAttackersEachCreateASpirit() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(2);
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("An indestructible attacker survives but still creates a Spirit with the specified characteristics")
    void indestructibleAttackerStillCreatesSpirit() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        addCreatureReady(player1, new TajicBladeOfTheLegion()).setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Tajic, Blade of the Legion");
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
        Permanent spirit = findPermanent(player2, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, spirit)).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Spirit is created even if the damaging creature dies before the trigger resolves")
    void departedAttackerStillCreatesSpirit() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        resolveCombat(player1);
        assertThat(gd.stack).isNotEmpty();
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to another player does not trigger Teysa")
    void damageToOpponentDoesNotTrigger() {
        harness.addToBattlefield(player1, new TeysaEnvoyOfGhosts());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Protection from creatures does not stop Teysa's untargeted destruction ability")
    void untargetedTriggerDestroysOpposingTeysa() {
        harness.addToBattlefield(player2, new TeysaEnvoyOfGhosts());
        addCreatureReady(player1, new TeysaEnvoyOfGhosts()).setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Teysa, Envoy of Ghosts");
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from creatures prevents a creature from blocking Teysa")
    void creaturesCannotBlockTeysa() {
        addCreatureReady(player1, new TeysaEnvoyOfGhosts()).setAttacking(true);
        harness.addToBattlefield(player2, new TajicBladeOfTheLegion());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from creatures allows a noncreature damage spell to target and damage Teysa")
    void noncreatureSpellCanDamageTeysa() {
        Permanent teysa = harness.addToBattlefieldAndReturn(player2, new TeysaEnvoyOfGhosts());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, teysa.getId());

        assertThat(teysa.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Teysa, Envoy of Ghosts");
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Vigilance lets Teysa attack without tapping")
    void attackingDoesNotTapTeysa() {
        Permanent teysa = addCreatureReady(player1, new TeysaEnvoyOfGhosts());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(teysa.isAttacking()).isTrue();
        assertThat(teysa.isTapped()).isFalse();
    }
}
