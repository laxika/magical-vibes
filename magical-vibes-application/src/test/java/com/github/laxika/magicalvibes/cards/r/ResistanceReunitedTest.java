package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResistanceReunitedTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target creature and protects your equipped creatures")
    void boostsTargetAndProtectsEquippedCreatures() {
        Permanent target = addCreature(player1);
        Permanent equipped = addCreature(player1);
        addEquipment(player1, equipped);
        Permanent unequipped = addCreature(player1);
        Permanent opponentEquipped = addCreature(player2);
        addEquipment(player2, opponentEquipped);

        castResolve(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unequipped, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEquipped, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and indestructible grant wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = addCreature(player1);
        Permanent equipped = addCreature(player1);
        addEquipment(player1, equipped);

        castResolve(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player1.getId()).add(fountain);
        harness.setHand(player1, List.of(new ResistanceReunited()));
        addMana();

        UUID targetId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addEquipment(Player player, Permanent host) {
        Permanent equipment = new Permanent(new BoneSaw());
        equipment.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(equipment);
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new ResistanceReunited()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
