package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcingdeathFrostTongue.class, GrizzlyBears.class})
class IcingdeathFrostTongueTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {2} attaches Frost Tongue and gives the creature +2/+0")
    void equipsAndBoostsCreature() {
        Permanent equipment = addReady(player1, new IcingdeathFrostTongue());
        Permanent creature = addReady(player1, new GrizzlyBears());
        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(equipment);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, equipmentIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps a target creature defending player controls when the equipped creature attacks")
    void attackTriggerTapsDefendingCreature() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent equipment = addReady(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(attacker.getId());
        Permanent victim = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the attacking player")
    void cannotTargetOwnCreature() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent equipment = addReady(player1, new IcingdeathFrostTongue());
        equipment.setAttachedTo(attacker.getId());
        Permanent ownCreature = addReady(player1, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
