package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BirthrightBoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KellanTheFaeBlooded.class, BirthrightBoon.class, GrizzlyBears.class,
        HolyStrength.class, LeoninScimitar.class})
class KellanTheFaeBloodedTest extends BaseCardTest {

    @Test
    void otherCreaturesGetPowerForEachAuraAndEquipmentAttachedToKellan() {
        Permanent kellan = addCreatureReady(player1, new KellanTheFaeBlooded());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addCreatureReady(player1, new HolyStrength());
        Permanent equipment = addCreatureReady(player1, new LeoninScimitar());
        aura.setAttachedTo(kellan.getId());
        equipment.setAttachedTo(kellan.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kellan)).isEqualTo(5);
    }

    @Test
    void birthrightBoonSearchesForAnAuraOrEquipmentAndPutsItIntoHand() {
        Card aura = new HolyStrength();
        Card equipment = new LeoninScimitar();
        Card creature = new GrizzlyBears();
        KellanTheFaeBlooded card = new KellanTheFaeBlooded();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(creature, aura, equipment));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getType)
                .containsExactlyInAnyOrder(CardType.ENCHANTMENT, CardType.ARTIFACT);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, equipment);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void bonusUpdatesWhenEquipmentMovesAndDoesNotBoostOpponents() {
        Permanent kellan = addCreatureReady(player1, new KellanTheFaeBlooded());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        equipment.setAttachedTo(kellan.getId());
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, other.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(other.getId());
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(2);
    }

    @Test
    void opponentControlledAuraAttachedToKellanCountsForTheBonus() {
        Permanent kellan = addCreatureReady(player1, new KellanTheFaeBlooded());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(kellan.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, kellan)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    void birthrightBoonCanChooseEquipmentThenKellanCanBeCastFromExile() {
        KellanTheFaeBlooded card = new KellanTheFaeBlooded();
        Card aura = new HolyStrength();
        Card equipment = new LeoninScimitar();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(aura, equipment));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kellan, the Fae-Blooded");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void birthrightBoonCanFailToFindEvenWithAnEligibleCard() {
        KellanTheFaeBlooded card = new KellanTheFaeBlooded();
        Card aura = new HolyStrength();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void birthrightBoonResolvesWithNoEligibleCards() {
        KellanTheFaeBlooded card = new KellanTheFaeBlooded();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
