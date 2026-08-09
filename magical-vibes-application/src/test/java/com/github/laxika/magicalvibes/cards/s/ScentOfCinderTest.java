package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScentOfCinderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of revealed red cards")
    void dealsDamageForRevealedRedCards() {
        Shock shock = new Shock();
        LightningBolt lightningBolt = new LightningBolt();
        Counterspell blueCard = new Counterspell();
        harness.setHand(player1, List.of(new ScentOfCinder(), shock, lightningBolt, blueCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(shock.getId(), lightningBolt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), lightningBolt.getId()));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Revealing zero red cards deals no damage")
    void revealingZeroRedCardsDealsNoDamage() {
        harness.setHand(player1, List.of(new ScentOfCinder(), new Shock(), new Counterspell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ScentOfCinder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
