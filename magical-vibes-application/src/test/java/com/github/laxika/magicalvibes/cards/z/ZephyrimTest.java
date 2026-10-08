package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Zephyrim.class)
class ZephyrimTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new Zephyrim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zephyrim")).hasSize(3);
        assertThat(findPermanents(player1, "Zephyrim"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Drawing Zephyrim as the first card offers its miracle cost")
    void firstDrawOffersMiracle() {
        harness.setLibrary(player1, List.of(new Zephyrim()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("A later draw this turn does not offer miracle")
    void laterDrawDoesNotOfferMiracle() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.setLibrary(player1, List.of(new Zephyrim()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    @DisplayName("Casting without squad payments creates no copies")
    void castingWithoutSquadCreatesNoCopies() {
        harness.setHand(player1, List.of(new Zephyrim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zephyrim")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Entering without being cast creates no squad copies")
    void enteringWithoutCastingCreatesNoCopies() {
        harness.enterBattlefieldAndReturn(player1, new Zephyrim());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zephyrim")).hasSize(1);
    }

    @Test
    @DisplayName("Accepting miracle casts Zephyrim for one generic and one white mana")
    void miracleCastsForReducedCost() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.setLibrary(player1, List.of(new Zephyrim()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();

            assertThat(findPermanents(player1, "Zephyrim")).hasSize(1);
            harness.assertNotInHand(player1, "Zephyrim");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Declining miracle casting leaves Zephyrim in hand and spends no mana")
    void decliningMiracleCastLeavesCardInHand() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.setLibrary(player1, List.of(new Zephyrim()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            harness.assertInHand(player1, "Zephyrim");
            harness.assertNotOnBattlefield(player1, "Zephyrim");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        });
    }
}
