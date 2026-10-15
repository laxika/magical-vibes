package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmorphousAxe.class, MotherBear.class})
class AmorphousAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0 and every creature type")
    void equippedCreatureGetsBoostAndEveryCreatureType() {
        Permanent creature = addCreatureReady(player1, new MotherBear());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.BEAR)).isEqualTo(1);
        assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isEqualTo(1);
        assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.SLIVER)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Amorphous Axe can equip a creature for {3}")
    void equipsCreature() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new MotherBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equipped creature loses the Axe's effects when it is removed")
    void effectsEndWhenAxeLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new MotherBear());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(axe);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.CHANGELING)).isFalse();
        assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isZero();
    }

    @Test
    @DisplayName("Re-equipping moves the effects only when the ability resolves")
    void reequippingMovesEffectsOnResolution() {
        Permanent axe = addAxeReady(player1);
        Permanent first = addCreatureReady(player1, new MotherBear());
        Permanent second = addCreatureReady(player1, new MotherBear());
        axe.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(axe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player2, new MotherBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(axe.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void cannotEquipDuringCombat() {
        addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new MotherBear());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("An illegal equip target leaves the Axe attached to its previous creature")
    void failedReequipPreservesPreviousAttachment() {
        Permanent axe = addAxeReady(player1);
        Permanent first = addCreatureReady(player1, new MotherBear());
        Permanent second = addCreatureReady(player1, new MotherBear());
        axe.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.countControlledSubtypePermanents(gd, player1.getId(), CardSubtype.ELF)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAxeReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new AmorphousAxe());
    }
}
