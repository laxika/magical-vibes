package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParadiseMantle.class, AuriokChampion.class})
class ParadiseMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can tap for a chosen color")
    void equippedCreatureCanTapForChosenColor() {
        Permanent creature = addCreatureReady(player1);
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the creature equipped with Paradise Mantle has its mana ability")
    void onlyEquippedCreatureGetsManaAbility() {
        Permanent equippedCreature = addCreatureReady(player1);
        Permanent otherCreature = addCreatureReady(player1);
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(equippedCreature.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(equippedCreature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Equip {1} attaches Paradise Mantle to a creature you control")
    void equipAttachesToCreature() {
        Permanent mantle = addMantleReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void equipRequiresCreatureYouControl() {
        addMantleReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("An unattached creature does not have Paradise Mantle's mana ability")
    void unattachedCreatureDoesNotHaveManaAbility() {
        addCreatureReady(player1);
        addMantleReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The granted mana ability produces any color without using the stack")
    void manaAbilityProducesEachColorImmediately(ManaColor color) {
        Permanent creature = addCreatureReady(player1);
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(color)).isZero();
        assertThat(creature.isTapped()).isTrue();
        assertThat(mantle.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Re-equipping transfers the mana ability to the new creature")
    void reEquipTransfersManaAbility() {
        Permanent original = addCreatureReady(player1);
        Permanent replacement = addCreatureReady(player1);
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(mantle.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(original.isTapped()).isFalse();
        assertThat(replacement.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick creature cannot activate the granted tap ability")
    void summoningSicknessPreventsManaActivation() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AuriokChampion());
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted mana ability cannot be activated twice without untapping")
    void tappedCreatureCannotActivateAgain() {
        Permanent creature = addCreatureReady(player1);
        Permanent mantle = addMantleReady(player1);
        mantle.setAttachedTo(creature.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipRequiresSorceryTiming() {
        Permanent mantle = addMantleReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(mantle.getAttachedTo()).isNull();
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new AuriokChampion());
    }

    private Permanent addMantleReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ParadiseMantle());
        perm.setSummoningSick(false);
        return perm;
    }
}
