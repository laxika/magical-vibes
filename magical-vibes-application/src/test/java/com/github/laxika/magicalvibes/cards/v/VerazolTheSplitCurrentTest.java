package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LlanowarElite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerazolTheSplitCurrent.class, LlanowarElite.class})
class VerazolTheSplitCurrentTest extends BaseCardTest {

    @Test
    void entersWithCountersForManaSpent() {
        castVerazol();

        Permanent verazol = findPermanent(player1, "Verazol, the Split Current");
        assertThat(verazol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void mayRemoveTwoCountersToCopyKickedPermanentSpellAsToken() {
        castVerazol();
        castKickedElite();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> elites = findPermanents(player1, "Llanowar Elite");
        assertThat(elites).hasSize(2);
        assertThat(elites).anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void decliningDoesNotRemoveCountersOrCopySpell() {
        castVerazol();
        castKickedElite();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Llanowar Elite")).hasSize(1);
        assertThat(findPermanent(player1, "Verazol, the Split Current")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void castVerazol() {
        harness.setHand(player1, List.of(new VerazolTheSplitCurrent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void castKickedElite() {
        harness.setHand(player1, List.of(new LlanowarElite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castKickedCreature(player1, 0);
    }
}
