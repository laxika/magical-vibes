package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrongholdMachinistTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell by paying mana, tapping, and discarding a card")
    void countersNoncreatureSpell() {
        Permanent machinist = addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 0, null, shock.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(machinist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, bears.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new StrongholdMachinist());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, shock.getId())
        ).isInstanceOf(IllegalStateException.class);
    }
}
