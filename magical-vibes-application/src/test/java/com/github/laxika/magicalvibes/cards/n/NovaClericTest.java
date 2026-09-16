package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CrudeRampart;
import com.github.laxika.magicalvibes.cards.w.WordsOfWar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NovaCleric.class, WordsOfWar.class, CrudeRampart.class})
class NovaClericTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing itself destroys all enchantments but not creatures")
    void sacrificesItselfAndDestroysAllEnchantments() {
        addCreatureReady(player1, new NovaCleric());
        harness.addToBattlefield(player1, new WordsOfWar());
        harness.addToBattlefield(player2, new WordsOfWar());
        harness.addToBattlefield(player2, new CrudeRampart());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Nova Cleric");
        harness.assertOnBattlefield(player1, "Words of War");
        harness.assertOnBattlefield(player2, "Words of War");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Words of War");
        harness.assertInGraveyard(player2, "Words of War");
        harness.assertOnBattlefield(player2, "Crude Rampart");
    }

    @Test
    @DisplayName("Cannot activate without {2}{W}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new NovaCleric());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nova Cleric");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new NovaCleric());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nova Cleric");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
