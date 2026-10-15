package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoothsayerAdept.class, Forest.class})
class SoothsayerAdeptTest extends BaseCardTest {

    @Test
    void activatingAndResolvingDrawsThenDiscards() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SoothsayerAdept());
        adept.setSummoningSick(false);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(adept.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void canDiscardTheNewlyDrawnCard() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SoothsayerAdept());
        adept.setSummoningSick(false);
        Forest original = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, drawn);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyHandStillDrawsAndDiscards() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SoothsayerAdept());
        adept.setSummoningSick(false);
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SoothsayerAdept());
        adept.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(adept.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new SoothsayerAdept());
        adept.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(adept.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
