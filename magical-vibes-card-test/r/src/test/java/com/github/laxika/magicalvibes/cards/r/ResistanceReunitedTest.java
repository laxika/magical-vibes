package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoldwardensHelm;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResistanceReunited.class, CopperLonglegs.class, GoldwardensHelm.class, PropheticPrism.class})
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

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
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

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new ResistanceReunited()));
        addMana();

        UUID targetId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canBoostOpponentsCreatureWithoutProtectingIt() {
        Permanent target = addCreature(player2);
        addEquipment(player2, target);
        Permanent equipped = addCreature(player1);
        addEquipment(player1, equipped);

        castResolve(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void equippedTargetReceivesBothEffectsEvenWithOpponentsEquipment() {
        Permanent target = addCreature(player1);
        addEquipment(player2, target);

        castResolve(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void protectionDoesNotMoveWhenEquipmentMovesAfterResolution() {
        Permanent target = addCreature(player1);
        Permanent initiallyEquipped = addCreature(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldwardensHelm());
        equipment.setAttachedTo(initiallyEquipped.getId());

        castResolve(target);
        equipment.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, initiallyEquipped, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void equipmentAttachedBeforeResolutionQualifiesForProtection() {
        Permanent target = addCreature(player1);
        Permanent equipped = addCreature(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldwardensHelm());
        harness.setHand(player1, List.of(new ResistanceReunited()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        equipment.setAttachedTo(equipped.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, equipped, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void illegalTargetPreventsProtectionForOtherEquippedCreatures() {
        Permanent target = addCreature(player1);
        Permanent equipped = addCreature(player1);
        addEquipment(player1, equipped);
        harness.setHand(player1, List.of(new ResistanceReunited()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, equipped, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Resistance Reunited");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CopperLonglegs());
    }

    private void addEquipment(Player player, Permanent host) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new GoldwardensHelm());
        equipment.setAttachedTo(host.getId());
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new ResistanceReunited()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
