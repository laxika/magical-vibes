package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ManaCrypt.class)
class ManaCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mana Crypt produces two colorless mana")
    void tappingProducesTwoColorlessMana() {
        Permanent manaCrypt = harness.addToBattlefieldAndReturn(player1, new ManaCrypt());
        manaCrypt.setSummoningSick(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(manaCrypt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At the beginning of your upkeep, Mana Crypt flips a coin and damages you on a loss")
    void upkeepCoinFlipDamagesControllerOnLoss() {
        harness.addToBattlefield(player1, new ManaCrypt());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip for Mana Crypt");
        boolean lost = gameLogContains("loses the coin flip for Mana Crypt");

        assertThat(won).isNotEqualTo(lost);
        harness.assertLife(player1, lost ? 17 : 20);
    }

    @Test
    @DisplayName("Mana Crypt does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ManaCrypt());
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gameLogContains("coin flip for Mana Crypt")).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Mana Crypt's mana ability resolves immediately while its upkeep trigger waits")
    void manaAbilityCanBeUsedBeforeUpkeepTriggerResolves() {
        Permanent manaCrypt = harness.addToBattlefieldAndReturn(player1, new ManaCrypt());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("coin flip for Mana Crypt")).isFalse();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(manaCrypt.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip for Mana Crypt");
        boolean lost = gameLogContains("loses the coin flip for Mana Crypt");
        assertThat(won).isNotEqualTo(lost);
        harness.assertLife(player1, lost ? 17 : 20);
        harness.assertLife(player2, 20);
    }
}
