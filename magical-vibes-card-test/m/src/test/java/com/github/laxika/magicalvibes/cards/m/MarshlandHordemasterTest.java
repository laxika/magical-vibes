package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshlandHordemaster.class, GrizzlyBears.class, Shock.class, Conspiracy.class})
class MarshlandHordemasterTest extends BaseCardTest {

    @Test
    @DisplayName("A Lizard entering perpetually gives Marshland Hordemaster battle cry")
    void lizardEntryPerpetuallyGrantsBattleCry() {
        Permanent hordemaster = harness.enterBattlefieldAndReturn(player1, new MarshlandHordemaster());
        harness.passBothPriorities();
        hordemaster.setSummoningSick(false);

        assertThat(gqs.hasKeyword(gd, hordemaster, Keyword.BATTLE_CRY)).isTrue();

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hordemaster)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("When Marshland Hordemaster or a Lizard dies, target opponent loses 1 life and you gain 1 life")
    void lizardDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new MarshlandHordemaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID hordemasterId = harness.getPermanentId(player1, "Marshland Hordemaster");
        harness.castAndResolveInstant(player2, 0, hordemasterId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A non-Lizard creature dying does not trigger Marshland Hordemaster")
    void nonLizardDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new MarshlandHordemaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int opponentLife = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    @DisplayName("Each Lizard entry grants another independently triggering battle cry")
    void repeatedLizardEntriesStackBattleCry() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new MarshlandHordemaster());
        resolveAllTriggers();
        Permanent second = harness.enterBattlefieldAndReturn(player1, new MarshlandHordemaster());
        resolveAllTriggers();
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("An allied Lizard death triggers both the survivor and the dying Hordemaster")
    void alliedLizardDeathDrainsForEachHordemaster() {
        harness.addToBattlefield(player1, new MarshlandHordemaster());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new MarshlandHordemaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, dying.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opposing Lizard entering does not grant battle cry")
    void opposingLizardEntryDoesNotGrantBattleCry() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new MarshlandHordemaster());

        harness.enterBattlefieldAndReturn(player2, new MarshlandHordemaster());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, own, Keyword.BATTLE_CRY)).isFalse();
    }

    @Test
    @DisplayName("An allied non-Lizard entering does not grant battle cry")
    void nonLizardEntryDoesNotGrantBattleCry() {
        Permanent hordemaster = harness.addToBattlefieldAndReturn(player1, new MarshlandHordemaster());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, hordemaster, Keyword.BATTLE_CRY)).isFalse();
    }

    @Test
    @DisplayName("An opposing Lizard death does not trigger your Hordemaster")
    void opposingLizardDeathDoesNotDrainForYourHordemaster() {
        harness.addToBattlefield(player1, new MarshlandHordemaster());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new MarshlandHordemaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, opposing.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Its own entry grants battle cry even when its creature types are replaced")
    void selfEntryDoesNotRequireLizardType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent hordemaster = harness.enterBattlefieldAndReturn(player1, new MarshlandHordemaster());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, hordemaster, Keyword.BATTLE_CRY)).isTrue();
    }

    @Test
    @DisplayName("Perpetually granted battle cry survives death and a return to the battlefield")
    void battleCryPersistsAcrossZoneChanges() {
        Permanent hordemaster = harness.enterBattlefieldAndReturn(player1, new MarshlandHordemaster());
        resolveAllTriggers();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hordemaster.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Marshland Hordemaster");
        Card returnedCard = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.setGraveyard(player1, List.of());
        Permanent returned = addCreatureReady(player1, returnedCard);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }
}
