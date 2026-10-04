package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FecundGreenshell.class, Forest.class, GiantSpider.class, GrizzlyBears.class, FlowstoneSurge.class})
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
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new FecundGreenshell(), "{3}{G}{G}");
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
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player1, new FecundGreenshell());
        harness.castFromHand(player1, new GiantSpider(), "{3}{G}");
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
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player1, new FecundGreenshell());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void nonlandTopCardGoesIntoHand() {
        GrizzlyBears topCard = new GrizzlyBears();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.castFromHand(player1, new FecundGreenshell(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void declinedLandGoesIntoHand() {
        Forest topCard = new Forest();
        GrizzlyBears nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.castFromHand(player1, new FecundGreenshell(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void equalEffectivePowerAndToughnessDoesNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new FecundGreenshell());
        harness.addToBattlefield(player1, new FlowstoneSurge());
        harness.castFromHand(player1, new GiantSpider(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void bonusTracksLandThresholdAndOnlyAffectsControllersCreatures() {
        Permanent shell = harness.addToBattlefieldAndReturn(player1, new FecundGreenshell());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, shell)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        Permanent tenthLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(tenthLand);
        assertThat(gqs.getEffectivePower(gd, shell)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shell)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
    }

    @Test
    void opponentsCreatureDoesNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new FecundGreenshell());
        harness.enterBattlefieldAndReturn(player2, new GiantSpider());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void emptyLibraryDoesNotRequestChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new FecundGreenshell(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

}
