package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FendOff;
import com.github.laxika.magicalvibes.cards.t.TemporalAdept;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishLookout.class, FendOff.class, TemporalAdept.class})
class ElvishLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Elvish Lookout cannot be targeted by an opponent's spell")
    void opponentSpellCannotTargetIt() {
        harness.forceActivePlayer(player2);
        Permanent lookout = addCreatureReady(player1, new ElvishLookout());
        harness.setHand(player2, List.of(new FendOff()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, lookout.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Elvish Lookout cannot be targeted by its controller's spell")
    void ownSpellCannotTargetIt() {
        Permanent lookout = addCreatureReady(player1, new ElvishLookout());
        harness.setHand(player1, List.of(new FendOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, lookout.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Elvish Lookout cannot be targeted by an activated ability")
    void activatedAbilityCannotTargetIt() {
        Permanent lookout = addCreatureReady(player1, new ElvishLookout());
        addCreatureReady(player2, new TemporalAdept());
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, lookout.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
