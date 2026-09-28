package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingScorchbeast.class, Millstone.class, GrizzlyBears.class, Forest.class})
class ScreechingScorchbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives each player two rad counters")
    void attackingGivesEachPlayerTwoRadCounters() {
        addCreatureReady(player1, new ScreechingScorchbeast());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonland milling may create that many Zombie Mutant tokens once each turn")
    void nonlandMillingCreatesTokensOnceEachTurn() {
        addCreatureReady(player1, new ScreechingScorchbeast());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie Mutant")).hasSize(2);

        harness.activateAbility(player1, 2, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Zombie Mutant")).hasSize(2);
    }

    @Test
    @DisplayName("Milling only lands does not trigger token creation")
    void millingOnlyLandsDoesNotTrigger() {
        addCreatureReady(player1, new ScreechingScorchbeast());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Zombie Mutant")).isEmpty();
    }
}
