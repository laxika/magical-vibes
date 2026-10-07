package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BehindTheMask;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurveillanceMonitor.class, BehindTheMask.class, FountainOfYouth.class, GrizzlyBears.class})
class SurveillanceMonitorTest extends BaseCardTest {

    @Test
    void entersAndMayCollectEvidenceToCreateAThopter() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.castFromHand(player1, new SurveillanceMonitor(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(thopter -> {
                    assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
                    assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
                    assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
                });
    }

    @Test
    void triggersWhenEvidenceIsCollectedByAnotherCard() {
        harness.addToBattlefield(player1, new SurveillanceMonitor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new BehindTheMask()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void cannotChooseCardsBelowTheEvidenceThreshold() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.castFromHand(player1, new SurveillanceMonitor(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enough total mana value");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }

    @Test
    void mayDeclineCollectingEvidenceEvenWithEnoughCards() {
        Card evidence = new SurveillanceMonitor();
        harness.setGraveyard(player1, List.of(evidence));
        harness.enterBattlefieldAndReturn(player1, new SurveillanceMonitor());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void cannotCollectEvidenceWhenGraveyardHasInsufficientManaValue() {
        Card evidence = new BehindTheMask();
        harness.setGraveyard(player1, List.of(evidence));
        harness.enterBattlefieldAndReturn(player1, new SurveillanceMonitor());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void enteringWithAnEmptyGraveyardCreatesNoToken() {
        harness.setGraveyard(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SurveillanceMonitor());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void eachMonitorTriggersOnceForAnEvidenceCollectionAboveTheThreshold() {
        harness.addToBattlefield(player1, new SurveillanceMonitor());
        Card first = new SurveillanceMonitor();
        Card second = new SurveillanceMonitor();
        harness.setGraveyard(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new SurveillanceMonitor());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
    }

    @Test
    void collectingEvidenceDoesNotTriggerTheOpponentsMonitor() {
        harness.addToBattlefield(player2, new SurveillanceMonitor());
        Card evidence = new SurveillanceMonitor();
        harness.setGraveyard(player1, List.of(evidence));
        harness.enterBattlefieldAndReturn(player1, new SurveillanceMonitor());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }
}
