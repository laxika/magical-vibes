package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FecundGreenshell.class, Forest.class, GiantSpider.class, GrizzlyBears.class})
class FecundGreenshellTest extends BaseCardTest {

    @Test
    void boostsCreaturesWhenControllerHasTenLands() {
        Permanent shell = harness.addToBattlefieldAndReturn(player1, new FecundGreenshell());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gqs.getEffectivePower(gd, shell)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, shell)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void selfEntryMayPutTopLandOntoBattlefieldTapped() {
        Forest forest = new Forest();
        harness.setLibrary(player1, deckOf(forest));
        harness.setHand(player1, List.of(new FecundGreenshell()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteredForest = findPermanent(player1, forest);
        assertThat(enteredForest.isTapped()).isTrue();
    }

    @Test
    void anotherCreatureWithGreaterToughnessMayPutTopLandOntoBattlefieldTapped() {
        Forest forest = new Forest();
        harness.setLibrary(player1, deckOf(forest));
        harness.addToBattlefield(player1, new FecundGreenshell());
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteredForest = findPermanent(player1, forest);
        assertThat(enteredForest.isTapped()).isTrue();
    }

    @Test
    void creatureWithoutGreaterToughnessDoesNotTrigger() {
        Forest forest = new Forest();
        harness.setLibrary(player1, deckOf(forest));
        harness.addToBattlefield(player1, new FecundGreenshell());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
