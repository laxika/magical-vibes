package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SnoopingNewsie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CementShoes.class, SnoopingNewsie.class})
class CementShoesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent shoes = addShoesReady(player1);
        shoes.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature is tapped at the beginning of its controller's end step")
    void tapsEquippedCreatureAtControllerEndStep() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent shoes = addShoesReady(player1);
        shoes.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature does not untap during its controller's untap step")
    void equippedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new SnoopingNewsie());
        creature.tap();
        Permanent shoes = addShoesReady(player2);
        shoes.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip {2} attaches Cement Shoes to a creature you control")
    void equipAttachesToCreature() {
        Permanent shoes = addShoesReady(player1);
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shoes.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void doesNotTapAtEquipmentControllersEndStepWhenCreatureHasDifferentController() {
        Permanent creature = addCreatureReady(player2, new SnoopingNewsie());
        Permanent shoes = addShoesReady(player1);
        shoes.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        assertThat(shoes.isTapped()).isFalse();
    }

    @Test
    void tapsAtCreatureControllersEndStepEvenWhenEquipmentHasDifferentController() {
        Permanent creature = addCreatureReady(player2, new SnoopingNewsie());
        Permanent shoes = addShoesReady(player1);
        shoes.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(shoes.isTapped()).isFalse();
    }

    @Test
    void movingEquipmentAfterTriggerDoesNotChangeWhichCreatureTaps() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent other = addCreatureReady(player1, new SnoopingNewsie());
        Permanent shoes = addShoesReady(player1);
        shoes.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(creature.isTapped()).isFalse();

        shoes.setAttachedTo(other.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    void movingEquipmentRemovesBoostAndUntapLockFromPreviousCreature() {
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        Permanent other = addCreatureReady(player1, new SnoopingNewsie());
        Permanent shoes = addShoesReady(player1);
        shoes.setAttachedTo(creature.getId());
        creature.tap();
        other.tap();

        shoes.setAttachedTo(other.getId());
        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent shoes = addShoesReady(player1);
        Permanent creature = addCreatureReady(player2, new SnoopingNewsie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shoes.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        Permanent shoes = addShoesReady(player1);
        Permanent creature = addCreatureReady(player1, new SnoopingNewsie());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shoes.getAttachedTo()).isNull();
    }

    private Permanent addShoesReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CementShoes());
        perm.setSummoningSick(false);
        return perm;
    }

}
