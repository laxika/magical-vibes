package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeeringEmblemTest extends BaseCardTest {

    // ===== Trigger: "Whenever you cast a spell, equipped creature gets +2/+2 until end of turn." =====

    @Test
    @DisplayName("Casting a spell gives the equipped creature +2/+2")
    void castingSpellBoostsEquippedCreature() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent emblem = addReady(player1, new LeeringEmblem());
        emblem.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent emblem = addReady(player1, new LeeringEmblem());
        emblem.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No trigger when the Equipment is not attached to a creature")
    void noTriggerWhenUnattached() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        addReady(player1, new LeeringEmblem()); // present but unattached

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Equip {2} =====

    @Test
    @DisplayName("Resolving equip attaches the Equipment to the target creature")
    void resolvingEquipAttaches() {
        Permanent emblem = addReady(player1, new LeeringEmblem());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(emblem.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
