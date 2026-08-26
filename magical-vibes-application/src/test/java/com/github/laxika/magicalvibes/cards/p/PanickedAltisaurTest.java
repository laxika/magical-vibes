package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PanickedAltisaur.class)
class PanickedAltisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 2 damage to each opponent")
    void tapAbilityDealsDamage() {
        addReadyAltisaur(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Tap ability taps Panicked Altisaur")
    void tapAbilityTapsCreature() {
        Permanent altisaur = addReadyAltisaur(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(altisaur.isTapped()).isTrue();
    }

    private Permanent addReadyAltisaur(Player player) {
        return addCreatureReady(player, new PanickedAltisaur());
    }
}
