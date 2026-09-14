package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.c.CinderSeer;
import com.github.laxika.magicalvibes.cards.f.FlameJet;
import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
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

@CardUsed({ScentOfCinder.class, FlameJet.class, CinderSeer.class, ScentOfBrine.class,
        BraidwoodCup.class, GoblinBerserker.class})
class ScentOfCinderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of revealed red cards")
    void dealsDamageForRevealedRedCards() {
        FlameJet firstRedCard = new FlameJet();
        CinderSeer secondRedCard = new CinderSeer();
        ScentOfBrine blueCard = new ScentOfBrine();
        harness.setHand(player1, List.of(new ScentOfCinder(), firstRedCard, secondRedCard, blueCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(firstRedCard.getId(), secondRedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstRedCard.getId(), secondRedCard.getId()));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Revealing zero red cards deals no damage")
    void revealingZeroRedCardsDealsNoDamage() {
        harness.setHand(player1, List.of(new ScentOfCinder(), new FlameJet(), new ScentOfBrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Resolves without a choice when the hand has no red cards")
    void resolvesWithoutChoiceWhenNoRedCardsInHand() {
        harness.setHand(player1, List.of(new ScentOfCinder(), new ScentOfBrine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals the revealed damage to a creature")
    void dealsDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoblinBerserker());
        FlameJet redCard = new FlameJet();
        harness.setHand(player1, List.of(new ScentOfCinder(), redCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(redCard.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BraidwoodCup());
        harness.setHand(player1, List.of(new ScentOfCinder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
