package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScytheSpecter.class, GrizzlyBears.class, Island.class})
class ScytheSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the opponent discard and lose life equal to the discarded card's mana value")
    void combatDamageCausesManaValueLifeLoss() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A zero-mana-value discard causes no life loss")
    void zeroManaValueDiscardCausesNoLifeLoss() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new Island())));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent with no cards to discard loses no life")
    void emptyHandCausesNoLifeLoss() {
        Permanent specter = addCreatureReady(player1, new ScytheSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
