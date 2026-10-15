package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DragonHatchling;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaigamOjutaiMaster.class, Cancel.class, Divination.class, DragonHatchling.class,
        GrizzlyBears.class, DoomBlade.class, TeferisProtection.class})
class TaigamOjutaiMasterTest extends BaseCardTest {

    @Test
    void controllerInstantSorceryAndDragonSpellsCannotBeCountered() {
        harness.addToBattlefield(player1, new TaigamOjutaiMaster());

        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, divination.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Cancel");

        DragonHatchling dragon = new DragonHatchling();
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, dragon.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dragon Hatchling");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void nonDragonCreatureSpellCanBeCountered() {
        harness.addToBattlefield(player1, new TaigamOjutaiMaster());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void instantOrSorceryFromHandGainsReboundAfterTaigamAttacks() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);

        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void spellDoesNotGainReboundBeforeTaigamAttacks() {
        harness.addToBattlefield(player1, new TaigamOjutaiMaster());

        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(divination.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    void reboundStillAppliesWhenTaigamDiesInResponseToItsTrigger() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, taigam.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Taigam, Ojutai Master");
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundCanBeCastForFreeNextUpkeepAndDoesNotReboundAgain() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(divination.getId())).isNull();
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOpportunity() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
    }

    @Test
    void opponentInstantCanBeCounteredByProtectedControllerInstant() {
        harness.addToBattlefield(player1, new TaigamOjutaiMaster());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DoomBlade doomBlade = new DoomBlade();
        Cancel cancel = new Cancel();
        harness.setHand(player1, List.of(cancel));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(doomBlade, new Cancel()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, doomBlade.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, cancel.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Doom Blade");
        harness.assertInGraveyard(player1, "Cancel");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void dragonDoesNotGainReboundEvenAfterTaigamAttacks() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);
        DragonHatchling dragon = new DragonHatchling();
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dragon Hatchling");
        assertThat(gd.findExiledCard(dragon.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void spellThatExilesItselfDoesNotCreateReboundOpportunity() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);
        TeferisProtection protection = new TeferisProtection();
        harness.setHand(player1, List.of(protection));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(protection.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void instantFromHandGainsReboundAfterTaigamAttacks() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DoomBlade doomBlade = new DoomBlade();
        harness.setHand(player1, List.of(doomBlade));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(doomBlade.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void spellWithAllTargetsIllegalDoesNotReboundDespiteCounterProtection() {
        Permanent taigam = harness.addToBattlefieldAndReturn(player1, new TaigamOjutaiMaster());
        taigam.setAttackedThisTurn(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DoomBlade doomBlade = new DoomBlade();
        harness.setHand(player1, List.of(doomBlade));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Doom Blade");
        assertThat(gd.findExiledCard(doomBlade.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }
}
