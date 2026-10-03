package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnointWithAffliction;
import com.github.laxika.magicalvibes.cards.b.BladedAmbassador;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompleatDevotion.class, ChargeOfTheMites.class, BladedAmbassador.class,
        CrawlingChorus.class, AnointWithAffliction.class})
class CompleatDevotionTest extends BaseCardTest {

    @Test
    @DisplayName("Pumps a toxic creature and draws a card")
    void pumpsToxicCreatureAndDrawsCard() {
        Permanent mite = createToxicMite();
        harness.setLibrary(player1, List.of(new BladedAmbassador()));
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, mite.getId());

        assertThat(gqs.getEffectivePower(gd, mite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mite)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Bladed Ambassador");
    }

    @Test
    @DisplayName("Pumps a non-toxic creature without drawing")
    void pumpsNonToxicCreatureWithoutDrawing() {
        Permanent ambassador = harness.addToBattlefieldAndReturn(player1, new BladedAmbassador());
        harness.setLibrary(player1, List.of(new BladedAmbassador()));
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, ambassador.getId());

        assertThat(gqs.getEffectivePower(gd, ambassador)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ambassador)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only a creature you control")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BladedAmbassador());
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent chorus = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.setLibrary(player1, List.of(new BladedAmbassador()));
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, chorus.getId());

        assertThat(gqs.getEffectivePower(gd, chorus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chorus)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, chorus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chorus)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not draw when the toxic target is exiled in response")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent chorus = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        harness.setLibrary(player1, List.of(new BladedAmbassador()));
        harness.setHand(player1, List.of(new CompleatDevotion()));
        harness.setHand(player2, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, chorus.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, chorus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chorus);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent createToxicMite() {
        harness.setHand(player1, List.of(new ChargeOfTheMites()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        return findPermanent(player1, "Mite");
    }
}
