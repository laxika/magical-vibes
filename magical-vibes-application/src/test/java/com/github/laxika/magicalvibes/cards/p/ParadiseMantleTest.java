package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuriokChampion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new AuriokChampion());
    }

    private Permanent addMantleReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ParadiseMantle());
        perm.setSummoningSick(false);
        return perm;
    }
}
