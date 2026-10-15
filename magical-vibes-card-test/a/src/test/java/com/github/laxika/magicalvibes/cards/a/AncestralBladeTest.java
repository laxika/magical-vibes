package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
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

@CardUsed({AncestralBlade.class, GreenwoodSentinel.class, Disenchant.class})
class AncestralBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Ancestral Blade creates and equips a 1/1 Soldier token")
    void enteringCreatesAndEquipsSoldier() {
        harness.setHand(player1, List.of(new AncestralBlade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent blade = findPermanent(player1, "Ancestral Blade");
        Permanent soldier = findPermanent(player1, "Soldier");

        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(blade.getAttachedTo()).isEqualTo(soldier.getId());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {1} moves Ancestral Blade and its bonus to another creature")
    void equipMovesBladeAndBonus() {
        harness.setHand(player1, List.of(new AncestralBlade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent sentinel = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, sentinel.getId());
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Ancestral Blade");
        Permanent soldier = findPermanent(player1, "Soldier");

        assertThat(blade.getAttachedTo()).isEqualTo(sentinel.getId());
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Soldier is still created if Ancestral Blade leaves before its trigger resolves")
    void createsSoldierWhenBladeIsDestroyedInResponse() {
        harness.setHand(player1, List.of(new AncestralBlade()));
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent blade = findPermanent(player1, "Ancestral Blade");
        harness.assertNotOnBattlefield(player1, "Soldier");

        harness.castInstant(player2, 0, blade.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ancestral Blade");
        harness.assertNotOnBattlefield(player1, "Ancestral Blade");
        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new AncestralBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip is restricted to sorcery timing")
    void cannotEquipDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new AncestralBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip pays one generic mana and does not create another Soldier")
    void equipPaysGenericManaWithoutCreatingToken() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new AncestralBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        resolveAllTriggers();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Soldier");
    }
}
