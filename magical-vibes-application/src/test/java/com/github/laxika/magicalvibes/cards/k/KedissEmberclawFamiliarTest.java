package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KedissEmberclawFamiliar.class, GrizzlyBears.class})
class KedissEmberclawFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when a commander you control deals combat damage")
    void triggersForCommanderCombatDamage() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player1, commander);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(1));
            harness.passBothPriorities();
        });

        assertThat(gameLogContains("Kediss, Emberclaw Familiar's triggered ability goes on the stack."))
                .isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a noncommander creature")
    void doesNotTriggerForNoncommanderCombatDamage() {
        addCreatureReady(player1, new KedissEmberclawFamiliar());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gameLogContains("Kediss, Emberclaw Familiar's triggered ability goes on the stack."))
                .isFalse();
    }
}
