package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EntreatTheAngels.class})
class EntreatTheAngelsTest extends BaseCardTest {

    private List<Permanent> angels() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Angel"))
                .toList();
    }

    @Test
    @DisplayName("Cast for {X}{X}{W}{W}{W} with X=2 creates two 4/4 flying Angels")
    void hardCastCreatesXAngels() {
        harness.setHand(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(angels()).hasSize(2);
        assertThat(angels()).allSatisfy(angel -> {
            assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
            assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        // X is paid twice: 2*2 generic + {W}{W}{W}.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cast with X=0 creates no tokens")
    void hardCastWithZeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(angels()).isEmpty();
    }

    @Test
    @DisplayName("Miracle cast for {X}{W}{W} announces X and creates that many Angels")
    void miracleCastAnnouncesX() {
        harness.setLibrary(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal
        harness.passBothPriorities(); // resolve miracle trigger → cast prompt
        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AlternateCastXValueChoice.class);

        harness.handleXValueChosen(player1, 3);
        harness.passBothPriorities(); // resolve Entreat the Angels

        assertThat(angels()).hasSize(3);
        // {3}{W}{W} paid out of 2 white + 3 colorless.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Miracle cast with X=0 creates no tokens and spends only the coloured mana")
    void miracleCastWithZero() {
        harness.setLibrary(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(angels()).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void decliningRevealKeepsCardInHandWithoutSpendingMana() {
        EntreatTheAngels card = new EntreatTheAngels();
        harness.setLibrary(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
        assertThat(angels()).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void decliningMiracleCastKeepsRevealedCardInHand() {
        EntreatTheAngels card = new EntreatTheAngels();
        harness.setLibrary(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
        assertThat(angels()).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void secondCardDrawnThisTurnDoesNotOfferMiracle() {
        EntreatTheAngels first = new EntreatTheAngels();
        EntreatTheAngels second = new EntreatTheAngels();
        harness.setLibrary(player1, List.of(first, second));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void miracleCanBeCastOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new EntreatTheAngels()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(angels()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
