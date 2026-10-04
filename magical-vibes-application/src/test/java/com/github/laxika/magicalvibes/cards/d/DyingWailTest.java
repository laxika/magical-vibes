package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DyingWail.class, FountainOfYouth.class, GiantSpider.class, GrizzlyBears.class})
class DyingWailTest extends BaseCardTest {

    @Test
    void enchantedCreatureDeathTargetsPlayerAndMakesThemDiscardTwoCards() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent dyingWail = harness.addToBattlefieldAndReturn(player1, new DyingWail());
        dyingWail.setAttachedTo(spider.getId());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new FountainOfYouth(), new GiantSpider())));

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void canBeCastOnCreatureAndCanMakeItsControllerDiscardTwoCards() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new DyingWail()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        Permanent dyingWail = findPermanent(player1, "Dying Wail");
        assertThat(dyingWail.getAttachedTo()).isEqualTo(spider.getId());

        harness.setHand(player1, List.of(new GrizzlyBears(), new FountainOfYouth()));
        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void discardsAsManyAsPossibleFromShortHand(int handSize) {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent dyingWail = harness.addToBattlefieldAndReturn(player1, new DyingWail());
        dyingWail.setAttachedTo(spider.getId());
        harness.setHand(player2, handSize == 0 ? List.of() : List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FountainOfYouth()));

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        if (handSize == 1) {
            harness.handleCardChosen(player2, 0);
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dying Wail");
    }
    @Test
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DyingWail()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature");
    }
}
