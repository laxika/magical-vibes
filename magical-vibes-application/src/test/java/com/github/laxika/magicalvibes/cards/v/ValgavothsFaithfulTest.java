package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValgavothsFaithful.class, GrizzlyBears.class, Mountain.class})
class ValgavothsFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and returns a creature from the graveyard")
    void sacrificesItselfAndReturnsCreature() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana(player1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Valgavoth's Faithful");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Valgavoth's Faithful");
    }

    @Test
    @DisplayName("Requires a creature card as the graveyard target")
    void rejectsNonCreatureGraveyardTarget() {
        Card land = new Mountain();
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        harness.setGraveyard(player1, List.of(land));
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Valgavoth's Faithful");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        harness.addToBattlefield(player1, new ValgavothsFaithful());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
