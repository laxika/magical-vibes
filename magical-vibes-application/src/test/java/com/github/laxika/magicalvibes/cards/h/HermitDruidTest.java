package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HermitDruid.class, Forest.class, Shock.class, VolrathsStronghold.class})
class HermitDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first basic land into hand and the rest into the graveyard")
    void putsBasicLandToHandRestToGraveyard() {
        Permanent druid = addReadyDruid(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card shock = new Shock();
        Card forest = new Forest();
        Card leftover = new Shock();
        harness.setLibrary(player1, List.of(shock, forest, leftover));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(leftover);
    }

    @Test
    @DisplayName("Nonbasic lands do not stop the reveal")
    void nonbasicLandDoesNotStopReveal() {
        addReadyDruid(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card stronghold = new VolrathsStronghold();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(stronghold, forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(stronghold);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Whole library goes to graveyard when no basic land is revealed")
    void noBasicLandMillsEntireLibrary() {
        addReadyDruid(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card shock = new Shock();
        Card secondShock = new Shock();
        harness.setLibrary(player1, List.of(shock, secondShock));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock, secondShock);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when library is empty")
    void emptyLibraryDoesNothing() {
        addReadyDruid(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setLibrary(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int gyBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(gyBefore);
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutMana() {
        addReadyDruid(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent perm = new Permanent(new HermitDruid());
        perm.setSummoningSick(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(perm);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent druid = addReadyDruid(player1);
        druid.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDruid(Player player) {
        return addCreatureReady(player, new HermitDruid());
    }
}
