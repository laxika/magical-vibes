package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheSpearOfLeonidas;
import com.github.laxika.magicalvibes.cards.u.UmezawasJitte;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KassandraEagleBearer.class, GrizzlyBears.class, TheSpearOfLeonidas.class,
        UmezawasJitte.class})
class KassandraEagleBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with The Spear of Leonidas from the graveyard")
    void searchesGraveyardForTheSpearOfLeonidas() {
        harness.setGraveyard(player1, List.of(new TheSpearOfLeonidas()));
        harness.setHand(player1, List.of(new KassandraEagleBearer()));

        castKassandra();
        resolveKassandraEnterTrigger();

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
        harness.assertNotInGraveyard(player1, "The Spear of Leonidas");
    }

    @Test
    @DisplayName("Searches the library for The Spear of Leonidas")
    void searchesLibraryForTheSpearOfLeonidas() {
        harness.setLibrary(player1, List.of(new TheSpearOfLeonidas()));
        harness.setHand(player1, List.of(new KassandraEagleBearer()));

        castKassandra();
        resolveKassandraEnterTrigger();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "The Spear of Leonidas");
    }

    @Test
    @DisplayName("Draws when a creature with a legendary Equipment attached deals combat damage")
    void drawsForCombatDamageWithLegendaryEquipment() {
        addCreatureReady(player1, new KassandraEagleBearer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent jitte = addEquipment(player1, new UmezawasJitte());
        jitte.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw for a nonlegendary Equipment")
    void doesNotDrawForNonlegendaryEquipment() {
        addCreatureReady(player1, new KassandraEagleBearer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addEquipment(player1, nonlegendaryEquipment());
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void castKassandra() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void resolveKassandraEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addEquipment(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card nonlegendaryEquipment() {
        Card card = new Card();
        card.setName("Test Equipment");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{1}");
        card.setSupertypes(Set.of());
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        return card;
    }
}
