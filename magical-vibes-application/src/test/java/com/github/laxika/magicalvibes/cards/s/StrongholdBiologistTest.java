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

@CardUsed({StrongholdBiologist.class, AccumulatedKnowledge.class, Mossdog.class})
class StrongholdBiologistTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell by paying mana, tapping, and discarding a card")
    void countersCreatureSpell() {
        Permanent biologist = addCreatureReady(player1, new StrongholdBiologist());
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Mossdog spell = new Mossdog();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{G}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 0, null, spell.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Accumulated Knowledge");
        harness.assertInGraveyard(player2, "Mossdog");
        assertThat(biologist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        addCreatureReady(player1, new StrongholdBiologist());
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
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
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new StrongholdBiologist());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Mossdog spell = new Mossdog();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, spell.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying {U}{U}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new StrongholdBiologist());
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
    @DisplayName("Cannot activate while Stronghold Biologist is tapped")
    void cannotActivateWhileTapped() {
        Permanent biologist = addCreatureReady(player1, new StrongholdBiologist());
        biologist.tap();
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
