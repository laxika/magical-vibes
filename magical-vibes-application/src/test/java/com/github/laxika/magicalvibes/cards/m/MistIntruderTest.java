package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistIntruder.class, GrizzlyBears.class})
class MistIntruderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent intruder = addAttackingIntruder(player1);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isNull();
        assertThat(gd.getCardsExiledByPermanent(intruder.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card is exiled when the damaged player's library is empty")
    void noExileWhenLibraryEmpty() {
        addAttackingIntruder(player1);
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.exiledCards).isEmpty();
    }

    private Permanent addAttackingIntruder(Player player) {
        Permanent intruder = addCreatureReady(player, new MistIntruder());
        intruder.setAttacking(true);
        return intruder;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
