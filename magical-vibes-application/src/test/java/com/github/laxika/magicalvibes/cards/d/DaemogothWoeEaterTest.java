package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CastDown;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaemogothWoeEaterTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, it sacrifices itself and its sacrifice trigger resolves")
    void upkeepSacrificeTriggersDiscardDrawAndLifeGain() {
        Permanent daemogoth = harness.addToBattlefieldAndReturn(player1, new DaemogothWoeEater());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(daemogoth.getId()));
        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Destroying it without sacrificing it does not trigger its sacrifice ability")
    void destructionDoesNotTriggerSacrificeAbility() {
        Permanent daemogoth = harness.addToBattlefieldAndReturn(player1, new DaemogothWoeEater());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new CastDown())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 10);

        harness.castInstant(player1, 0, daemogoth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
