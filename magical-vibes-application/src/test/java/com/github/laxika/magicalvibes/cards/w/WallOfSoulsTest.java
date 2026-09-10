package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
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

@CardUsed({WallOfSouls.class, MorgueThrull.class, ChandraNalaar.class, Shock.class})
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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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
    @CardUsed(SpinedWurm.class)
    @DisplayName("Combat-damage trigger resolves after lethal damage destroys Wall of Souls")
    void reflectsLethalCombatDamageAfterWallDies() {
        Permanent wall = addCreatureReady(player2, new WallOfSouls());
        addCreatureReady(player1, new SpinedWurm());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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
}
