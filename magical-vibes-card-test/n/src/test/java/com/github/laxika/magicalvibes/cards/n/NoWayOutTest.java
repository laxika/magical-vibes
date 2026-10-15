package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoWayOut.class, Forest.class})
class NoWayOutTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two cards and the caster creates a decayed Zombie")
    void opponentDiscardsTwoAndCasterCreatesDecayedZombie() {
        harness.setHand(player2, new ArrayList<>(List.of(new NoWayOut(), new Forest())));
        castNoWayOut();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new NoWayOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castNoWayOut() {
        harness.setHand(player1, List.of(new NoWayOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("An opponent with an empty hand still allows the caster to create a Zombie")
    void createsZombieWhenOpponentHasNoCards() {
        harness.setHand(player2, List.of());

        castNoWayOut();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("An opponent with one card discards it and the caster still creates a Zombie")
    void discardsOnlyAvailableCardAndCreatesZombie() {
        harness.setHand(player2, List.of(new Forest()));

        castNoWayOut();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("The decayed Zombie deals combat damage and is sacrificed after attacking")
    void attackingZombieIsSacrificedAtEndOfCombat() {
        harness.setHand(player2, List.of());
        castNoWayOut();
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zombie)));
            resolveCombat();
            harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        });

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("The decayed Zombie remains on the battlefield when it does not attack")
    void nonattackingZombieSurvivesCombat() {
        harness.setHand(player2, List.of());
        castNoWayOut();

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            declareAttackers(List.of());
            harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        });

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("The opponent chooses exactly two cards from a larger hand")
    void opponentChoosesWhichTwoCardsToDiscard() {
        NoWayOut retainedCard = new NoWayOut();
        Forest firstDiscard = new Forest();
        Forest secondDiscard = new Forest();
        harness.setHand(player2, List.of(firstDiscard, retainedCard, secondDiscard));

        castNoWayOut();
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }
}
