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
}
