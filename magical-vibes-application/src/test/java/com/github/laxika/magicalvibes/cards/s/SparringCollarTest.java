package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SparringCollar.class, DrossCrocodile.class})
class SparringCollarTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has first strike")
    void equippedCreatureHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        Permanent collar = addCollarReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        collar.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Red ability attaches Sparring Collar at instant speed")
    void redAbilityAttachesAtInstantSpeed() {
        Permanent collar = addCollarReady(player1);
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Red ability can target only a creature controlled by its controller")
    void redAbilityRequiresControlledCreature() {
        addCollarReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DrossCrocodile());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip ability attaches Sparring Collar for one mana")
    void equipAbilityAttachesForOneMana() {
        Permanent collar = addCollarReady(player1);
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip ability can target only a creature controlled by its controller")
    void equipAbilityRequiresControlledCreature() {
        addCollarReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new DrossCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip ability can only be activated at sorcery speed")
    void equipAbilityRequiresSorcerySpeed() {
        addCollarReady(player1);
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Moving Sparring Collar transfers first strike to the new creature")
    void movingCollarTransfersFirstStrike() {
        Permanent collar = addCollarReady(player1);
        Permanent firstCreature = addCreatureReady(player1, new DrossCrocodile());
        Permanent secondCreature = addCreatureReady(player1, new DrossCrocodile());
        collar.setAttachedTo(firstCreature.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(collar.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent addCollarReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SparringCollar());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
