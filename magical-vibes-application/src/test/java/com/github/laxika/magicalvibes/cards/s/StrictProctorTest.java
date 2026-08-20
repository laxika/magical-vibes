package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EidolonOfBlossoms;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrictProctorTest extends BaseCardTest {

    @Test
    void countersTriggeredAbilityCausedByPermanentEntering() {
        harness.addToBattlefield(player1, new StrictProctor());
        harness.setHand(player2, List.of(new EidolonOfBlossoms()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Eidolon of Blossoms");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void controllerOfCausedAbilityMayPayTwoMana() {
        harness.addToBattlefield(player1, new StrictProctor());
        harness.setHand(player2, List.of(new EidolonOfBlossoms()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
