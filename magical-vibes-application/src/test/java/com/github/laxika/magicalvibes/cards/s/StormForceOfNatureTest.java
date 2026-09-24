package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormForceOfNature.class, DarkRitual.class, GrizzlyBears.class})
class StormForceOfNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage gives the next instant or sorcery spell storm")
    void combatDamageGivesNextInstantOrSorceryStorm() {
        addCreatureReady(player1, new StormForceOfNature());
        gd.recordSpellCast(player2.getId(), new DarkRitual());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell does not consume the storm grant")
    void creatureSpellDoesNotConsumeStormGrant() {
        addCreatureReady(player1, new StormForceOfNature());
        gd.recordSpellCast(player2.getId(), new DarkRitual());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .hasSize(2)
                .allMatch(entry -> entry.getCard().getName().equals("Dark Ritual"));
    }
}
