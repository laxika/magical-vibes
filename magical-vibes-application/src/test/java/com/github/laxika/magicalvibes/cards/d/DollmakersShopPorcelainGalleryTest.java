package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DollmakersShopPorcelainGallery.class)
class DollmakersShopPorcelainGalleryTest extends BaseCardTest {

    @Test
    void dollmakersShopCreatesOneToyForOneOrMoreNonToyAttackers() {
        castRoom(0);
        Permanent firstAttacker = addCreatureReady(player1, creature("First attacker"));
        Permanent secondAttacker = addCreatureReady(player1, creature("Second attacker"));

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Toy")).hasSize(1);
    }

    @Test
    void dollmakersShopDoesNotTriggerForToyAttackers() {
        castRoom(0);
        Permanent toy = addCreatureReady(player1, toy());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(toy)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Toy")).hasSize(1);
    }

    @Test
    void porcelainGallerySetsOwnCreaturesToTheirCount() {
        castRoom(1);
        Permanent firstCreature = addCreatureReady(player1, creature("First creature"));
        Permanent secondCreature = addCreatureReady(player1, creature("Second creature"));
        Permanent opponentCreature = addCreatureReady(player2, creature("Opponent creature"));

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);

        Permanent thirdCreature = addCreatureReady(player1, creature("Third creature"));

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thirdCreature)).isEqualTo(3);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new DollmakersShopPorcelainGallery()));
        harness.addMana(player1, WHITE, doorIndex == 0 ? 2 : 6);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private Card toy() {
        Card card = creature("Toy");
        card.setToken(true);
        card.setSubtypes(List.of(CardSubtype.TOY));
        return card;
    }
}
