package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jackhammer.class, ChromeCat.class})
class JackhammerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        jackhammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip ability attaches Jackhammer to a creature")
    void equipAttachesJackhammer() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(jackhammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature loses Jackhammer's boost when it is unattached")
    void creatureLosesBoostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        jackhammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        jackhammer.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void reequippingMovesBoostToNewCreature() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        Permanent first = addCreatureReady(player1, new ChromeCat());
        Permanent second = addCreatureReady(player1, new ChromeCat());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(jackhammer.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        Permanent creature = addCreatureReady(player2, new ChromeCat());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jackhammer.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipNonCreature() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jackhammer.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jackhammer.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipWithInsufficientMana() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jackhammer.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jackhammer.getAttachedTo()).isNull();
    }

    @Test
    void cannotEquipDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new Jackhammer());
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipWhileStackIsNotEmpty() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        Permanent creature = addCreatureReady(player1, new ChromeCat());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
        assertThat(jackhammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void illegalTargetLeavesPreviousAttachmentIntact() {
        Permanent jackhammer = harness.addToBattlefieldAndReturn(player1, new Jackhammer());
        Permanent first = addCreatureReady(player1, new ChromeCat());
        Permanent second = addCreatureReady(player1, new ChromeCat());
        jackhammer.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(jackhammer.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
    }
}
