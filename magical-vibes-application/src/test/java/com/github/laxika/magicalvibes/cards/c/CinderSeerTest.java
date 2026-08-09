package com.github.laxika.magicalvibes.cards.c;

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

class CinderSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of revealed red cards")
    void dealsDamageForRevealedRedCards() {
        Permanent seer = addReadySeer();
        Shock shock = new Shock();
        LightningBolt lightningBolt = new LightningBolt();
        Counterspell blueCard = new Counterspell();
        harness.setHand(player1, List.of(shock, lightningBolt, blueCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(shock.getId(), lightningBolt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), lightningBolt.getId()));

        harness.assertLife(player2, 18);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing zero red cards deals no damage")
    void revealingZeroRedCardsDealsNoDamage() {
        addReadySeer();
        Shock redCard = new Shock();
        harness.setHand(player1, List.of(redCard, new Counterspell()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadySeer();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Shock()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySeer() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new CinderSeer());
        seer.setSummoningSick(false);
        return seer;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
