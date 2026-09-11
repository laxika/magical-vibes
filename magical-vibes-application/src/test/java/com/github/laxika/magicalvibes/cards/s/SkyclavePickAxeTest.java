package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyclavePickAxe.class, GrizzlyBears.class, Forest.class})
class SkyclavePickAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control")
    void entersAttachedToTargetCreature() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new SkyclavePickAxe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent equipment = findEquipment();
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Landfall gives the equipped creature +2/+2 until end of turn")
    void landfallBoostsEquippedCreatureUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches Skyclave Pick-Axe to another creature")
    void equipAttachesToTargetCreature() {
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SkyclavePickAxe());
        equipment.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Cannot target an opponent's creature when entering")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new SkyclavePickAxe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent findEquipment() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SkyclavePickAxe)
                .findFirst()
                .orElseThrow();
    }
}
