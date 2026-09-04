package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrabrasksForgeTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Puts an oil counter on itself and creates a hasty trampling Horror")
    void createsHastyTramplingHorror() {
        harness.addToBattlefield(player1, new UrabrasksForge());

        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent forge = findPermanent(player1, "Urabrask's Forge");
        Permanent horror = findPermanent(player1, "Phyrexian Horror");

        assertThat(forge.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(1);
        assertThat(horror.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(gqs.hasKeyword(gd, horror, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, horror, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Token power increases with oil counters and the token is sacrificed at the next end step")
    void tokenScalesAndIsSacrificedAtEndStep() {
        harness.addToBattlefield(player1, new UrabrasksForge());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Phyrexian Horror")).isZero();

        advanceToCombat(player1);
        harness.passBothPriorities();

        Permanent forge = findPermanent(player1, "Urabrask's Forge");
        Permanent horror = findPermanent(player1, "Phyrexian Horror");
        assertThat(forge.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(1);
    }
}
