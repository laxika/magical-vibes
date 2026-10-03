package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PossessedGoat;
import com.github.laxika.magicalvibes.cards.u.UnableToScream;
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
import static com.github.laxika.magicalvibes.model.ManaColor.BLUE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DollmakersShopPorcelainGallery.class, PossessedGoat.class, UnableToScream.class})
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

    @Test
    void lockedShopDoesNotCreateToys() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new PossessedGoat());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Toy")).isEmpty();
    }

    @Test
    void lockedGalleryDoesNotSetBasePowerAndToughness() {
        castRoom(0);
        Permanent goat = addCreatureReady(player1, new PossessedGoat());
        addCreatureReady(player1, new PossessedGoat());

        assertThat(gqs.getEffectivePower(gd, goat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goat)).isEqualTo(1);
    }

    @Test
    void unlockingGalleryMakesNewToysIncreaseEveryCreaturesBasePowerAndToughness() {
        Permanent room = castRoom(0);
        Permanent goat = addCreatureReady(player1, new PossessedGoat());
        harness.addMana(player1, WHITE, 6);
        harness.unlockRoomDoor(player1, 0, 1);
        assertThat(room.isRoomFullyUnlocked()).isTrue();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(goat)));
        resolveAllTriggers();

        Permanent toy = findPermanent(player1, "Toy");
        assertThat(gqs.getEffectivePower(gd, goat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goat)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, toy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, toy)).isEqualTo(2);
    }

    @Test
    void shopTriggerStillResolvesAfterItsAttackerAndSourceLeave() {
        Permanent room = castRoom(0);
        Permanent attacker = addCreatureReady(player1, new PossessedGoat());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerBattlefields.get(player1.getId()).remove(room);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Toy")).hasSize(1);
    }

    @Test
    void galleryUpdatesWhenCreaturesLeave() {
        castRoom(1);
        Permanent survivor = addCreatureReady(player1, new PossessedGoat());
        Permanent other = addCreatureReady(player1, new PossessedGoat());
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
    }

    @Test
    void unlockingGalleryOverridesBasePowerAndToughnessSetAfterRoomEntered() {
        castRoom(0);
        Permanent goat = addCreatureReady(player1, new PossessedGoat());
        addCreatureReady(player1, new PossessedGoat());
        harness.setHand(player1, List.of(new UnableToScream()));
        harness.addMana(player1, BLUE, 1);
        harness.castEnchantment(player1, 0, goat.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, goat)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, goat)).isEqualTo(2);

        harness.addMana(player1, WHITE, 6);
        harness.unlockRoomDoor(player1, 0, 1);

        assertThat(gqs.getEffectivePower(gd, goat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goat)).isEqualTo(2);
    }
    @Test
    void galleryDoesNotOverrideAnAuraThatEntersAfterGalleryUnlocks() {
        castRoom(1);
        Permanent goat = addCreatureReady(player1, new PossessedGoat());
        addCreatureReady(player1, new PossessedGoat());
        harness.setHand(player1, List.of(new UnableToScream()));
        harness.addMana(player1, BLUE, 1);
        harness.castEnchantment(player1, 0, goat.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goat)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, goat)).isEqualTo(2);
    }

    @Test
    void shopDoesNotTriggerForOpponentsAttackers() {
        castRoom(0);
        Permanent attacker = addCreatureReady(player2, new PossessedGoat());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Toy")).isEmpty();
        assertThat(findPermanents(player2, "Toy")).isEmpty();
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
