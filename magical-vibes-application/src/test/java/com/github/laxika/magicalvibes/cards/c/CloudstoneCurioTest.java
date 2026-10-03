package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.f.FistsOfIronwood;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudstoneCurio.class, GrayscaledGharial.class, Island.class, BorosSignet.class, LastGasp.class,
        FistsOfIronwood.class})
class CloudstoneCurioTest extends BaseCardTest {

    @Test
    void acceptingTriggerReturnsAnotherPermanentSharingType() {
        harness.addToBattlefield(player1, new CloudstoneCurio());
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.addToBattlefield(player1, new Island());

        harness.castFromHand(player1, new GrayscaledGharial(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(existingCreature.getId());
        harness.handlePermanentChosen(player1, existingCreature.getId());

        harness.assertInHand(player1, "Grayscaled Gharial");
        harness.assertOnBattlefield(player1, "Grayscaled Gharial");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    void decliningTriggerLeavesPermanentsOnBattlefield() {
        harness.addToBattlefield(player1, new CloudstoneCurio());
        harness.addToBattlefield(player1, new GrayscaledGharial());

        harness.castFromHand(player1, new GrayscaledGharial(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grayscaled Gharial");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void acceptingTriggerWithNoMatchingPermanentDoesNothing() {
        harness.addToBattlefield(player1, new CloudstoneCurio());

        harness.castFromHand(player1, new GrayscaledGharial(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grayscaled Gharial");
    }

    @Test
    void landEntryCanReturnAnotherLand() {
        harness.addToBattlefield(player1, new CloudstoneCurio());
        Permanent existingLand = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(existingLand.getId());
        harness.handlePermanentChosen(player1, existingLand.getId());

        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    void triggerUsesEnteringPermanentTypeAfterItLeavesBattlefield() {
        harness.addToBattlefield(player1, new CloudstoneCurio());
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());

        harness.castFromHand(player1, new GrayscaledGharial(), "{U}");
        harness.passBothPriorities();
        Permanent enteringCreature = findPermanents(player1, "Grayscaled Gharial").get(1);

        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, enteringCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(existingCreature.getId());
        harness.handlePermanentChosen(player1, existingCreature.getId());

        harness.assertInHand(player1, "Grayscaled Gharial");
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
    }

    @Test
    void artifactEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new CloudstoneCurio());

        harness.castFromHand(player1, new BorosSignet(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Boros Signet");
    }

    @Test
    void tokenEntryStillAllowsReturningCreatureAfterTokenCeasesToExist() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.setHand(player1, List.of(new FistsOfIronwood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, existingCreature.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new CloudstoneCurio());
        harness.passBothPriorities();
        List<Permanent> saprolings = List.copyOf(findPermanents(player1, "Saproling"));
        assertThat(saprolings).hasSize(2);

        for (Permanent saproling : saprolings) {
            harness.setHand(player1, List.of(new LastGasp()));
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castInstant(player1, 0, saproling.getId());
            harness.passBothPriorities();
        }
        harness.assertNotOnBattlefield(player1, "Saproling");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(existingCreature.getId());
        harness.handlePermanentChosen(player1, existingCreature.getId());
        harness.assertInHand(player1, "Grayscaled Gharial");
    }

    @Test
    void creatureEntryCannotReturnOpponentsCreature() {
        harness.addToBattlefield(player1, new CloudstoneCurio());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.addToBattlefield(player2, new GrayscaledGharial());

        harness.castFromHand(player1, new GrayscaledGharial(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.assertOnBattlefield(player2, "Grayscaled Gharial");
    }

    @Test
    void opponentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new CloudstoneCurio());
        harness.addToBattlefield(player1, new GrayscaledGharial());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrayscaledGharial(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grayscaled Gharial");
    }
}
