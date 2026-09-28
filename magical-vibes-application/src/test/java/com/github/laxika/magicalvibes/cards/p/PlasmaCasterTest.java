package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlasmaCaster.class, GrizzlyBears.class})
class PlasmaCasterTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and attacking with it grants two energy")
    void boostsEquippedCreatureAndGrantsEnergyOnAttack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addReady(player1, new PlasmaCaster());
        equipment.setAttachedTo(attacker.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability exiles the blocker or deals it 1 damage")
    void coinFlipExilesOrDamagesBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addReady(player1, new PlasmaCaster());
        equipment.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player1, 1, null, blocker.getId());
        harness.passBothPriorities();

        boolean exiled = gd.exiledCards.stream().anyMatch(entry -> entry.card().getId().equals(blocker.getCard().getId()));
        boolean damaged = blocker.getMarkedDamage() == 1;
        assertThat(exiled ^ damaged).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    @DisplayName("The activated ability only targets a creature blocking the equipped creature")
    void cannotTargetNonBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addReady(player1, new PlasmaCaster());
        equipment.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(bystander.getMarkedDamage()).isZero();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
