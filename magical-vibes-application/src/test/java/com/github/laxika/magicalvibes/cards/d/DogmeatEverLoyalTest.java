package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DogmeatEverLoyal.class, Bonesplitter.class, GrizzlyBears.class, HolyStrength.class})
class DogmeatEverLoyalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling five and returning an Aura or Equipment")
    void millsFiveAndReturnsAuraOrEquipment() {
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DogmeatEverLoyal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(5)
                .doesNotContain(aura);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
    }

    @Test
    @DisplayName("Creates Junk when an enchanted or equipped creature attacks")
    void createsJunkForEnchantedOrEquippedAttackers() {
        addCreatureReady(player1, new DogmeatEverLoyal());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(enchanted.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }
}
