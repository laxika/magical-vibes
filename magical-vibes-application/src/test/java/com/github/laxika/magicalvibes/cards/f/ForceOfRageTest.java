package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ForceOfRage.class)
class ForceOfRageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two hasty trampling 3/1 Elemental tokens")
    void createsElementalTokens() {
        harness.setHand(player1, List.of(new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(2);
        assertThat(elementals).allSatisfy(elemental -> {
            assertThat(elemental.getCard().getPower()).isEqualTo(3);
            assertThat(elemental.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
        });
    }

    @Test
    @DisplayName("Sacrifices the tokens at the caster's next upkeep, not an opponent's")
    void sacrificesTokensAtCastersNextUpkeep() {
        harness.setHand(player1, List.of(new ForceOfRage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Elemental");
    }

    @Test
    @DisplayName("Can exile a red card to cast on an opponent's turn")
    void castsForAlternateCostOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ForceOfRage(), new ForceOfRage()));
        harness.passPriority(player2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, 1);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Force of Rage");
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);
    }
}
