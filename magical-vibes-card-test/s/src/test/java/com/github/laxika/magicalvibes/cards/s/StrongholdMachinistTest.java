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

@CardUsed({StrongholdMachinist.class, AccumulatedKnowledge.class, Mossdog.class, SealOfRemoval.class})
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

        AccumulatedKnowledge spell = new AccumulatedKnowledge();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{1}{U}");
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

        AccumulatedKnowledge spell = new AccumulatedKnowledge();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{1}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, spell.getId())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent machinist = addCreatureReady(player1, new StrongholdMachinist());
        machinist.setSummoningSick(true);
        harness.setHand(player1, List.of(new Mossdog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        AccumulatedKnowledge spell = new AccumulatedKnowledge();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{1}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, spell.getId())
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(machinist.isTapped()).isFalse();
        harness.assertInHand(player1, "Mossdog");
    }

    @Test
    @DisplayName("Can discard a creature and pays the discard before resolution")
    void canDiscardCreatureAsCost() {
        Permanent machinist = addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new Mossdog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        AccumulatedKnowledge spell = new AccumulatedKnowledge();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{1}{U}");
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, null, spell.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mossdog");
        harness.assertNotInHand(player1, "Mossdog");
        assertThat(machinist.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.assertNotInGraveyard(player2, "Accumulated Knowledge");

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Accumulated Knowledge");
    }

    @Test
    @DisplayName("Can counter its controller's own noncreature spell")
    void canCounterOwnSpell() {
        addCreatureReady(player1, new StrongholdMachinist());
        AccumulatedKnowledge spell = new AccumulatedKnowledge();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, spell, "{1}{U}");
        harness.setHand(player1, List.of(new Mossdog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 0, null, spell.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Accumulated Knowledge");
        harness.assertInGraveyard(player1, "Mossdog");
    }

    @Test
    @DisplayName("Can counter a noncreature permanent spell")
    void canCounterEnchantmentSpell() {
        addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new Mossdog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        SealOfRemoval spell = new SealOfRemoval();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{U}");
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, 0, null, spell.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Seal of Removal");
        harness.assertNotOnBattlefield(player2, "Seal of Removal");
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent machinist = addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new Mossdog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefield(player2, new SealOfRemoval());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, machinist.getId());
        var abilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, abilityId)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
        assertThat(machinist.isTapped()).isFalse();
        harness.assertInHand(player1, "Mossdog");
    }
}
