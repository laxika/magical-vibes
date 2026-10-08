package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CacheGrab;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranGhoulcaller.class, Regrowth.class, GrizzlyBears.class, CacheGrab.class})
class VeteranGhoulcallerTest extends BaseCardTest {

    @Test
    void conjuresDuplicateOfCardReturnedFromOwnGraveyardToHand() {
        harness.addToBattlefield(player1, new VeteranGhoulcaller());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Regrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, returned.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2)
                .anyMatch(card -> !card.getId().equals(returned.getId()));
    }

    @Test
    void doesNotTriggerWhenCardLeavesGraveyardForExile() {
        harness.addToBattlefield(player1, new VeteranGhoulcaller());
        Card exiled = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiled));

        harness.getPermanentRemovalService().removeCardFromGraveyardByIdForExile(gd, exiled.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void conjuresDuplicateOfNoncreatureCard() {
        harness.addToBattlefield(player1, new VeteranGhoulcaller());
        Card returned = new Regrowth();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Regrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, returned.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(returned);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(card -> card.getName().equals("Regrowth"));
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .doesNotHaveDuplicates();
    }

    @Test
    void eachControlledGhoulcallerConjuresOneDuplicateWithoutTriggeringOpponent() {
        harness.addToBattlefield(player1, new VeteranGhoulcaller());
        harness.addToBattlefield(player1, new VeteranGhoulcaller());
        harness.addToBattlefield(player2, new VeteranGhoulcaller());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Regrowth()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, returned.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(returned);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .doesNotHaveDuplicates();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void conjuresDuplicateWhenCacheGrabPutsMilledCardIntoHand() {
        harness.addToBattlefield(player1, new VeteranGhoulcaller());
        Card returned = new VeteranGhoulcaller();
        harness.setLibrary(player1, List.of(returned, new Regrowth(), new Regrowth(), new Regrowth()));
        harness.setHand(player1, List.of(new CacheGrab()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(returned);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(card -> card.getName().equals("Veteran Ghoulcaller"));
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .doesNotHaveDuplicates();
    }
}
