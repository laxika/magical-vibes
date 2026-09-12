package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.b.BrineSeer;
import com.github.laxika.magicalvibes.cards.f.FlameJet;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CinderSeer.class, FlameJet.class, HulkingOgre.class, BrineSeer.class, BraidwoodCup.class})
class CinderSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of revealed red cards")
    void dealsDamageForRevealedRedCards() {
        Permanent seer = addCreatureReady(player1, new CinderSeer());
        FlameJet firstRedCard = new FlameJet();
        HulkingOgre secondRedCard = new HulkingOgre();
        BrineSeer blueCard = new BrineSeer();
        harness.setHand(player1, List.of(firstRedCard, secondRedCard, blueCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(firstRedCard.getId(), secondRedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstRedCard.getId(), secondRedCard.getId()));

        harness.assertLife(player2, 18);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing zero red cards deals no damage")
    void revealingZeroRedCardsDealsNoDamage() {
        addCreatureReady(player1, new CinderSeer());
        FlameJet redCard = new FlameJet();
        harness.setHand(player1, List.of(redCard, new BrineSeer()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals damage to a target creature")
    void dealsDamageToTargetCreature() {
        addCreatureReady(player1, new CinderSeer());
        Permanent target = addCreatureReady(player2, new HulkingOgre());
        FlameJet redCard = new FlameJet();
        harness.setHand(player1, List.of(redCard, new BrineSeer()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(redCard.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals no damage when there are no red cards in hand")
    void dealsNoDamageWhenNoRedCardsAreInHand() {
        Permanent seer = addCreatureReady(player1, new CinderSeer());
        harness.setHand(player1, List.of(new BrineSeer()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new CinderSeer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());
        harness.setHand(player1, List.of(new FlameJet()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
