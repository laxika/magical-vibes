package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LashweedLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, Lashweed Lurker may put a target nonland permanent on top of its owner's library")
    void castTriggerPutsTargetOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new LashweedLurker()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castCreature(player1, 0);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lashweed Lurker");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves the target on the battlefield")
    void decliningCastTriggerLeavesTargetAlone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new LashweedLurker()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castCreature(player1, 0);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lashweed Lurker");
    }

    @Test
    @DisplayName("The cast trigger cannot target a land")
    void castTriggerCannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new LashweedLurker()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .doesNotContain(forestId);
    }

    @Test
    @DisplayName("Emerge sacrifices a creature and reduces the generic cost by its mana value")
    void emergeSacrificesCreatureAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID sacrificedId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new LashweedLurker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lashweed Lurker");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
