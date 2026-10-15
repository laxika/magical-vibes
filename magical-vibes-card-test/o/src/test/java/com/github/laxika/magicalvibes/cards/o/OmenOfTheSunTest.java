package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmenOfTheSun.class})
class OmenOfTheSunTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesTwoHumanSoldiersAndGainsLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new OmenOfTheSun()));
        addOmenMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void sacrificesToScryTwo() {
        OmenOfTheSun firstCard = new OmenOfTheSun();
        OmenOfTheSun secondCard = new OmenOfTheSun();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheSun());
        addOmenMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(omen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(omen.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(firstCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard);
    }

    @Test
    void canBeCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new OmenOfTheSun()));
        addOmenMana();
        harness.ensurePriority(player1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Omen of the Sun");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void enterTriggerStillResolvesAfterSacrificingOmen() {
        harness.setHand(player1, List.of(new OmenOfTheSun()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Omen of the Sun");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Omen of the Sun");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.getCard().isToken()).isTrue());
    }

    private void addOmenMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
