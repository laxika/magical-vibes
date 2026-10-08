package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.l.Lifelink;
import com.github.laxika.magicalvibes.cards.m.MorgueThrull;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfSouls.class, MorgueThrull.class, SpinedWurm.class, ChandraNalaar.class, Shock.class, Lifelink.class})
class WallOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents Wall of Souls from attacking")
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new WallOfSouls());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat damage to Wall of Souls deals that much damage to the chosen opponent")
    void reflectsCombatDamageToOpponent() {
        addCreatureReady(player2, new WallOfSouls());
        addCreatureReady(player1, new MorgueThrull());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player1.getId()).doesNotContain(player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Morgue Thrull");
    }

    @Test
    @DisplayName("Combat-damage trigger resolves after lethal damage destroys Wall of Souls")
    void reflectsLethalCombatDamageAfterWallDies() {
        Permanent wall = addCreatureReady(player2, new WallOfSouls());
        addCreatureReady(player1, new SpinedWurm());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player1.getId()).doesNotContain(player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wall);
    }

    @Test
    @DisplayName("Combat damage trigger may target a planeswalker")
    void reflectsCombatDamageToPlaneswalker() {
        addCreatureReady(player2, new WallOfSouls());
        addCreatureReady(player1, new MorgueThrull());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(chandra.getId())
                .doesNotContain(player2.getId())
                .doesNotContain(gd.playerBattlefields.get(player1.getId()).getFirst().getId());

        harness.handlePermanentChosen(player2, chandra.getId());
        resolveAllTriggers();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Wall of Souls")
    void ignoresNoncombatDamage() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSouls());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Wall of Souls"));

        assertThat(wall.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage can be reflected to the Wall controller's own planeswalker")
    void reflectsCombatDamageToOwnPlaneswalker() {
        addCreatureReady(player2, new WallOfSouls());
        addCreatureReady(player1, new MorgueThrull());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(chandra.getId(), player1.getId())
                .doesNotContain(player2.getId());

        harness.handlePermanentChosen(player2, chandra.getId());
        resolveAllTriggers();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Previously marked noncombat damage is not included in reflected lethal combat damage")
    void reflectsOnlyCurrentCombatDamageWhenPreviouslyDamaged() {
        Permanent wall = addCreatureReady(player2, new WallOfSouls());
        addCreatureReady(player1, new MorgueThrull());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.castAndResolveInstant(player1, 0, wall.getId());
        assertThat(wall.getMarkedDamage()).isEqualTo(2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player2, "Wall of Souls");
        harness.assertInGraveyard(player2, "Wall of Souls");
    }

    @Test
    @DisplayName("A Wall with lifelink still gains life from reflected damage after dying in combat")
    void retainsLifelinkForReflectedDamageAfterLethalCombat() {
        Permanent wall = addCreatureReady(player2, new WallOfSouls());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Lifelink());
        aura.setAttachedTo(wall.getId());
        addCreatureReady(player1, new SpinedWurm());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Wall of Souls");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 25);
    }
}
