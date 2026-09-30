package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(OrzhovGuildmage.class)
class OrzhovGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("White ability makes the target player gain 1 life")
    void targetPlayerGainsLife() {
        addGuildmage();
        addAbilityMana(ManaColor.WHITE);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("White ability can target its controller")
    void controllerCanGainLife() {
        addGuildmage();
        addAbilityMana(ManaColor.WHITE);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Black ability makes each player lose 1 life")
    void eachPlayerLosesLife() {
        addGuildmage();
        addAbilityMana(ManaColor.BLACK);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("White ability cannot target a permanent")
    void whiteAbilityRequiresPlayerTarget() {
        addGuildmage();
        addAbilityMana(ManaColor.WHITE);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                addCreatureReady(player2, new OrzhovGuildmage()).getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addGuildmage() {
        addCreatureReady(player1, new OrzhovGuildmage());
    }

    private void addAbilityMana(ManaColor coloredMana) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, coloredMana, 1);
    }
}
