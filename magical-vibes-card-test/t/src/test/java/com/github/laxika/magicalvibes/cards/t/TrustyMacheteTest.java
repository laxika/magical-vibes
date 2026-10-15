package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
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

@CardUsed({TrustyMachete.class, StoneworkPuma.class, IntoTheRoil.class})
class TrustyMacheteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Trusty Machete to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent machete = addMacheteReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(machete.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent machete = addMacheteReady(player1);
        machete.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipment does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent otherCreature = addCreatureReady(player1, new StoneworkPuma());
        Permanent machete = addMacheteReady(player1);
        machete.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip pays two generic mana and does not tap the Equipment")
    void equipPaysTwoGenericMana() {
        Permanent machete = addMacheteReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(machete.isTapped()).isFalse();
        assertThat(machete.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip cannot be activated with only one mana")
    void cannotEquipWithInsufficientMana() {
        Permanent machete = addMacheteReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(machete.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Re-equipping moves the bonus from the old creature to the new creature")
    void reEquippingMovesBonus() {
        Permanent machete = addMacheteReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new StoneworkPuma());
        Permanent newCreature = addCreatureReady(player1, new StoneworkPuma());
        machete.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        assertThat(machete.getAttachedTo()).isEqualTo(oldCreature.getId());
        harness.passBothPriorities();

        assertThat(machete.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, newCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot equip an opponent's creature")
    void cannotEquipOpponentsCreature() {
        addMacheteReady(player1);
        Permanent creature = addCreatureReady(player2, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot equip outside a main phase")
    void cannotEquipDuringCombat() {
        addMacheteReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot equip during an opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        addMacheteReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate equip while another equip ability is on the stack")
    void cannotEquipWithNonemptyStack() {
        addMacheteReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An illegal equip target leaves the Equipment attached to its previous creature")
    void targetLeavingDoesNotDetachEquipment() {
        Permanent machete = addMacheteReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new StoneworkPuma());
        Permanent target = addCreatureReady(player1, new StoneworkPuma());
        machete.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stonework Puma");
        assertThat(machete.getAttachedTo()).isEqualTo(oldCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, oldCreature)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addMacheteReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TrustyMachete());
        perm.setSummoningSick(false);
        return perm;
    }
}
