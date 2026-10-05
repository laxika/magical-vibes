package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ShatterskullSmashing;
import com.github.laxika.magicalvibes.cards.s.ShatterskullTheHammerPass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PelakkaPredation.class, PelakkaCaverns.class, HillGiant.class, GrizzlyBears.class, Forest.class,
        ShatterskullSmashing.class, ShatterskullTheHammerPass.class})
class PelakkaPredationTest extends BaseCardTest {

    @Test
    void choosesAndDiscardsAnOpponentCardWithManaValueAtLeastThree() {
        HillGiant expensiveCard = new HillGiant();
        GrizzlyBears cheapCard = new GrizzlyBears();
        Forest land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(cheapCard, expensiveCard, land)));
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(expensiveCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cheapCard, land);
    }

    @Test
    void excludesCardsWithManaValueBelowThree() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void cannotTargetTheController() {
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void choosesExactlyOneOfMultipleCardsAtTheManaValueBoundary() {
        PelakkaPredation first = new PelakkaPredation();
        PelakkaPredation second = new PelakkaPredation();
        harness.setHand(player2, List.of(first, second));
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseAnIneligibleCardWhenAnEligibleCardExists() {
        Forest land = new Forest();
        PelakkaPredation eligibleCard = new PelakkaPredation();
        harness.setHand(player2, List.of(land, eligibleCard));
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, eligibleCard);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(eligibleCard);
    }

    @Test
    void resolvesAgainstAnEmptyHandWithoutRequestingAChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Pelakka Predation");
    }

    @Test
    void treatsXAsZeroForCardsInTheOpponentsHand() {
        ShatterskullSmashing xCard = new ShatterskullSmashing();
        PelakkaPredation eligibleCard = new PelakkaPredation();
        harness.setHand(player2, List.of(xCard, eligibleCard));
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(xCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(eligibleCard);
    }

    @Test
    void landFaceCannotProduceManaWhileTapped() {
        harness.setHand(player1, List.of(new PelakkaPredation()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        gd.playerBattlefields.get(player1.getId()).getFirst().untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landFaceEntersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new PelakkaPredation()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(PelakkaCaverns.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
