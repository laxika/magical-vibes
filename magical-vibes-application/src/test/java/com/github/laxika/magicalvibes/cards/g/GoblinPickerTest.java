package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinPicker.class, Forest.class, GrizzlyBears.class})
class GoblinPickerTest extends BaseCardTest {

    @Test
    void discardingADrawsAndTapsGoblinPicker() {
        Permanent picker = addReadyPicker();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(picker.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        addReadyPicker();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPicker() {
        Permanent picker = harness.addToBattlefieldAndReturn(player1, new GoblinPicker());
        picker.setSummoningSick(false);
        return picker;
    }
}
