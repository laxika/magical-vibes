package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.f.FurnaceSkullbomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagneticSnuffler.class, DarksteelAxe.class, FurnaceSkullbomb.class, GrizzlyBears.class})
class MagneticSnufflerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target Equipment from the graveyard and attaches it")
    void etbReturnsTargetEquipmentAndAttachesIt() {
        DarksteelAxe axe = new DarksteelAxe();
        harness.setGraveyard(player1, List.of(axe));
        harness.setHand(player1, List.of(new MagneticSnuffler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(axe.getId()));
        harness.passBothPriorities();

        Permanent snuffler = findPermanent(player1, "Magnetic Snuffler");
        Permanent returnedAxe = findPermanent(player1, "Darksteel Axe");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(axe);
        assertThat(returnedAxe.getAttachedTo()).isEqualTo(snuffler.getId());
    }

    @Test
    @DisplayName("ETB does not target a non-Equipment card")
    void etbDoesNotTargetNonEquipment() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MagneticSnuffler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing an artifact puts a +1/+1 counter on Magnetic Snuffler")
    void sacrificingArtifactAddsCounter() {
        Permanent snuffler = harness.addToBattlefieldAndReturn(player1, new MagneticSnuffler());
        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null);
        resolveAllTriggers();

        assertThat(snuffler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Furnace Skullbomb");
        assertThat(skullbomb.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An opponent's Equipment is not a legal graveyard target")
    void etbDoesNotReturnOpponentsEquipment() {
        harness.setGraveyard(player2, List.of(new DarksteelAxe()));
        harness.setHand(player1, List.of(new MagneticSnuffler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Darksteel Axe");
        harness.assertNotOnBattlefield(player1, "Darksteel Axe");
    }

    @Test
    @DisplayName("Equipment still returns unattached if Snuffler leaves before its trigger resolves")
    void equipmentReturnsWhenSourceHasLeft() {
        DarksteelAxe axe = new DarksteelAxe();
        harness.setGraveyard(player1, List.of(axe));
        harness.setHand(player1, List.of(new MagneticSnuffler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(axe.getId()));

        Permanent snuffler = findPermanent(player1, "Magnetic Snuffler");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, snuffler));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Darksteel Axe").getAttachedTo()).isNull();
        harness.assertNotInGraveyard(player1, "Darksteel Axe");
        harness.assertInGraveyard(player1, "Magnetic Snuffler");
    }

    @Test
    @DisplayName("An opponent's artifact sacrifice does not add a counter")
    void opponentsArtifactSacrificeDoesNotAddCounter() {
        Permanent snuffler = harness.addToBattlefieldAndReturn(player1, new MagneticSnuffler());
        harness.addToBattlefield(player2, new FurnaceSkullbomb());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 0, null);
        resolveAllTriggers();

        assertThat(snuffler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Furnace Skullbomb");
    }

    @Test
    @DisplayName("Destroying an artifact is not sacrificing it")
    void artifactDestructionDoesNotAddCounter() {
        Permanent snuffler = harness.addToBattlefieldAndReturn(player1, new MagneticSnuffler());
        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new FurnaceSkullbomb());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, skullbomb));
        resolveAllTriggers();

        assertThat(snuffler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Furnace Skullbomb");
    }

    @Test
    @DisplayName("Each artifact sacrifice adds a counter")
    void repeatedArtifactSacrificesEachAddCounter() {
        Permanent snuffler = harness.addToBattlefieldAndReturn(player1, new MagneticSnuffler());
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        harness.addToBattlefield(player1, new FurnaceSkullbomb());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 1, 0, null);
        resolveAllTriggers();

        assertThat(snuffler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
