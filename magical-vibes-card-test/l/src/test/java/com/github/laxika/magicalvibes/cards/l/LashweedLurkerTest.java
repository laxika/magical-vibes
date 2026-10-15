package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LashweedLurker.class, GrizzlyBears.class, Forest.class})
class LashweedLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, Lashweed Lurker may put a target nonland permanent on top of its owner's library")
    void castTriggerPutsTargetOnTopOfOwnersLibrary() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

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
        UUID forestId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LashweedLurker()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .doesNotContain(forestId);
    }

    @Test
    @DisplayName("Emerge sacrifices a creature and reduces the generic cost by its mana value")
    void emergeSacrificesCreatureAndReducesCost() {
        UUID sacrificedId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

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

    @Test
    void emergeReductionExceedingGenericCostStillPaysColoredMana() {
        UUID sacrificedId = harness.addToBattlefieldAndReturn(player1, new LashweedLurker()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LashweedLurker()).getId();
        harness.setHand(player1, List.of(new LashweedLurker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lashweed Lurker");
        harness.assertOnBattlefield(player1, "Lashweed Lurker");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void castingWithoutLegalTargetsStillResolvesCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new LashweedLurker()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lashweed Lurker");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDoesNotTrigger() {
        harness.addToBattlefield(player2, new LashweedLurker());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new LashweedLurker());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Lashweed Lurker");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }
}
