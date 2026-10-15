package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DreamstoneHedron.class)
class DreamstoneHedronTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dreamstone Hedron adds three colorless mana")
    void tapsForThreeColorlessMana() {
        Permanent hedron = harness.addToBattlefieldAndReturn(player1, new DreamstoneHedron());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(hedron.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three mana and sacrificing Dreamstone Hedron draws three cards")
    void sacrificesAndDrawsThreeCards() {
        harness.addToBattlefield(player1, new DreamstoneHedron());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Dreamstone Hedron");
        harness.assertInGraveyard(player1, "Dreamstone Hedron");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("The draw ability cannot be activated without three mana")
    void cannotActivateDrawAbilityWithoutEnoughMana() {
        harness.addToBattlefield(player1, new DreamstoneHedron());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dreamstone Hedron");
    }

    @Test
    @DisplayName("A tapped Dreamstone Hedron cannot produce mana again")
    void cannotActivateManaAbilityWhileTapped() {
        Permanent hedron = harness.addToBattlefieldAndReturn(player1, new DreamstoneHedron());
        hedron.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dreamstone Hedron");
    }

    @Test
    @DisplayName("Mana produced by Dreamstone Hedron cannot pay for its own draw ability while it is tapped")
    void cannotActivateDrawAbilityAfterTappingForMana() {
        harness.addToBattlefield(player1, new DreamstoneHedron());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dreamstone Hedron");
        harness.assertNotInGraveyard(player1, "Dreamstone Hedron");
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Colored mana pays the generic draw cost and only the controller draws")
    void coloredManaPaysForDrawAbility() {
        harness.addToBattlefield(player1, new DreamstoneHedron());
        harness.addMana(player1, ManaColor.BLUE, 3);
        DreamstoneHedron first = new DreamstoneHedron();
        DreamstoneHedron second = new DreamstoneHedron();
        DreamstoneHedron third = new DreamstoneHedron();
        harness.setLibrary(player1, List.of(first, second, third));

        GameData gd = harness.getGameData();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertInGraveyard(player1, "Dreamstone Hedron");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }
}
