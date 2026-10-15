package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisterRepentia.class, LightningBolt.class})
class SisterRepentiaTest extends BaseCardTest {

    @Test
    @DisplayName("When Sister Repentia dies, its controller gains 2 life and draws two cards")
    void diesGainsLifeAndDrawsTwoCards() {
        Permanent sister = harness.addToBattlefieldAndReturn(player1, new SisterRepentia());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0, sister.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.assertInGraveyard(player1, "Sister Repentia");
    }

    @Test
    @DisplayName("Drawing Sister Repentia as the first card of the turn offers its miracle cost")
    void firstDrawOffersMiracleCast() {
        SisterRepentia sister = new SisterRepentia();
        harness.setLibrary(player1, List.of(sister));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sister.getId()));
    }

    @Test
    @DisplayName("Miracle casts Sister Repentia for exactly one white and one black mana on the opponent's turn")
    void miracleCastOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new SisterRepentia()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            drawAndOfferMiracle();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotInHand(player1, "Sister Repentia");
            harness.assertNotOnBattlefield(player1, "Sister Repentia");
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

            harness.passBothPriorities();
            harness.assertOnBattlefield(player1, "Sister Repentia");
        });
    }

    @Test
    @DisplayName("Declining the miracle reveal leaves Sister Repentia in hand")
    void decliningRevealLeavesCardInHand() {
        harness.setLibrary(player1, List.of(new SisterRepentia()));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            drawAndOfferMiracle();
            harness.handleMayAbilityChosen(player1, false);

            harness.assertInHand(player1, "Sister Repentia");
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("Revealing Sister Repentia does not oblige its controller to cast it")
    void decliningMiracleCastLeavesCardAndMana() {
        harness.setLibrary(player1, List.of(new SisterRepentia()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            drawAndOfferMiracle();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            harness.assertInHand(player1, "Sister Repentia");
            harness.assertNotOnBattlefield(player1, "Sister Repentia");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Sister Repentia drawn second in a turn does not offer miracle")
    void secondDrawDoesNotOfferMiracle() {
        harness.setLibrary(player1, List.of(new SisterRepentia(), new SisterRepentia()));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            drawAndOfferMiracle();
            harness.handleMayAbilityChosen(player1, false);
            drawAndOfferMiracle();

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.pendingMayAbilities).isEmpty();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        });
    }

    @Test
    @DisplayName("Miracle cannot substitute generic mana for its required black mana")
    void miracleRequiresBothColors() {
        harness.setLibrary(player1, List.of(new SisterRepentia()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            drawAndOfferMiracle();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertInHand(player1, "Sister Repentia");
            harness.assertNotOnBattlefield(player1, "Sister Repentia");
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        });
    }

    private void drawAndOfferMiracle() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
    }
}
