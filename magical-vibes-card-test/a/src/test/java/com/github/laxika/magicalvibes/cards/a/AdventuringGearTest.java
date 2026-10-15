package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

@CardUsed({AdventuringGear.class, Forest.class, GrizzlyBears.class, Naturalize.class})
class AdventuringGearTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Adventuring Gear to a creature")
    void equipsCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Landfall gives the equipped creature +2/+2 until end of turn")
    void landfallBoostsEquippedCreatureUntilEndOfTurn() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gear.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall does not boost an unattached creature")
    void landfallDoesNotBoostUnattachedCreature() {
        addGearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall still boosts the last equipped creature after Gear is destroyed")
    void landfallUsesLastEquippedCreatureAfterGearLeaves() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gear.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Forest(), new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.playLand(player1, 0);
        harness.castAndResolveInstant(player1, 0, gear.getId());
        harness.assertInGraveyard(player1, "Adventuring Gear");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's land does not trigger Adventuring Gear")
    void opponentsLandDoesNotTrigger() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gear.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        addGearReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void cannotEquipDuringCombat() {
        addGearReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addGearReady(Player player) {
        Permanent gear = harness.addToBattlefieldAndReturn(player, new AdventuringGear());
        gear.setSummoningSick(false);
        return gear;
    }
}
