package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurderousCut.class, AlpineGrizzly.class, Mountain.class})
class MurderousCutTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and destroys the target creature")
    void delvesAndDestroysCreature() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        List<Card> graveyard = List.of(new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly(), new AlpineGrizzly());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Alpine Grizzly");
        harness.castInstantWithMultipleGraveyardExile(player1, 0, targetId, List.of(0, 1, 2, 3));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Murderous Cut");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayEntireCostWithManaWithoutDelving() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        Card graveyardCard = new Mountain();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Alpine Grizzly"));

        harness.assertNotOnBattlefield(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Murderous Cut");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canCombineManaAndDelveAndExileNoncreatureCards() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        Card first = new Mountain();
        Card unselected = new AlpineGrizzly();
        Card last = new MurderousCut();
        harness.setGraveyard(player1, List.of(first, unselected, last));
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0,
                harness.getPermanentId(player2, "Alpine Grizzly"), List.of(2, 0));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, last);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player2, "Alpine Grizzly");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Murderous Cut");
    }

    @Test
    void delveCannotPayBlackManaRequirement() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        List<Card> graveyard = List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(player1, 0,
                harness.getPermanentId(player2, "Alpine Grizzly"), List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Murderous Cut");
        harness.assertOnBattlefield(player2, "Alpine Grizzly");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotExileMoreCardsThanGenericCost() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        List<Card> graveyard = List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain(), new Mountain());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(player1, 0,
                harness.getPermanentId(player2, "Alpine Grizzly"), List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Murderous Cut");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotCountSameGraveyardCardTwiceForDelve() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        List<Card> graveyard = List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(player1, 0,
                harness.getPermanentId(player2, "Alpine Grizzly"), List.of(0, 0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Murderous Cut");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void delveCostsStayPaidWhenTargetDiesBeforeResolution() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        List<Card> graveyard = List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new MurderousCut()));
        harness.setHand(player2, List.of(new MurderousCut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 5);
        UUID targetId = harness.getPermanentId(player2, "Alpine Grizzly");

        harness.castInstantWithMultipleGraveyardExile(player1, 0, targetId, List.of(0, 1, 2, 3));
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Murderous Cut");
        harness.assertInGraveyard(player1, "Murderous Cut");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
