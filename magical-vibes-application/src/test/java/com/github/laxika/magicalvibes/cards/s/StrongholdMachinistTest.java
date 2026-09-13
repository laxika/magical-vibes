package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AccumulatedKnowledge;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrongholdMachinist.class, AccumulatedKnowledge.class, Mossdog.class})
class StrongholdMachinistTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell by paying mana, tapping, and discarding a card")
    void countersNoncreatureSpell() {
        Permanent machinist = addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        AccumulatedKnowledge spell = new AccumulatedKnowledge();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{1}{U}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 0, null, spell.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Accumulated Knowledge");
        harness.assertInGraveyard(player2, "Accumulated Knowledge");
        assertThat(machinist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Mossdog mossdog = new Mossdog();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, mossdog, "{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, mossdog.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        AccumulatedKnowledge spell = new AccumulatedKnowledge();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{1}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, spell.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying {U}{U}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));

        Mossdog spell = new Mossdog();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, spell.getId())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while Stronghold Machinist is tapped")
    void cannotActivateWhileTapped() {
        Permanent machinist = addCreatureReady(player1, new StrongholdMachinist());
        machinist.tap();
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Mossdog spell = new Mossdog();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, spell.getId())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
