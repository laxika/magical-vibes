package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hijack;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurderousSpoils.class, GrizzlyBears.class, LoxodonWarhammer.class, MassOfGhouls.class,
        Hijack.class})
class MurderousSpoilsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature and gains control of all Equipment attached to it")
    void destroysCreatureAndGainsAttachedEquipment() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent attachedEquipment = addEquipment(player2, target, new LoxodonWarhammer());
        Permanent attachedEquipmentFromCaster = addEquipment(player1, target, new LoxodonWarhammer());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent unrelatedEquipment = addEquipment(player2, otherCreature, new LoxodonWarhammer());

        castMurderousSpoils(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(attachedEquipment, attachedEquipmentFromCaster);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unrelatedEquipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attachedEquipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attachedEquipmentFromCaster);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(unrelatedEquipment);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = addCreatureReady(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new MurderousSpoils()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The creature cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        castMurderousSpoils(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Permanent control of attached Equipment overrides temporary control")
    void permanentControlOverridesTemporaryControl() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = addEquipment(player2, target, new LoxodonWarhammer());

        harness.setHand(player1, List.of(new Hijack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, equipment.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);

        castMurderousSpoils(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(equipment);
    }

    private void castMurderousSpoils(Permanent target) {
        harness.setHand(player1, List.of(new MurderousSpoils()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private Permanent addEquipment(Player player,
                                   Permanent creature, Card equipmentCard) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, equipmentCard);
        equipment.setAttachedTo(creature.getId());
        return equipment;
    }
}
