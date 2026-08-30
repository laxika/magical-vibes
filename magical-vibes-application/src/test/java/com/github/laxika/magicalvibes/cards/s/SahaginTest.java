package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sahagin.class, Shock.class, Hurricane.class, GrizzlyBears.class})
class SahaginTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell with less than four mana spent does not trigger Sahagin")
    void cheapNoncreatureSpellDoesNotTrigger() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(1);
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell with at least four mana spent adds a counter and makes Sahagin unblockable")
    void expensiveNoncreatureSpellTriggers() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(2);
        assertThat(sahagin.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(2);
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A creature spell does not trigger Sahagin")
    void creatureSpellDoesNotTrigger() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(1);
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
