package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ErtaisTrickery;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornplateIntimidator.class, Forest.class, GrizzlyBears.class, ErtaisTrickery.class})
class ThornplateIntimidatorTest extends BaseCardTest {

    @Test
    void targetOpponentLosesThreeLifeWhenNoAlternativeIsAvailable() {
        harness.setHand(player2, List.of());
        castIntimidator(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void targetOpponentMaySacrificeNonlandPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castIntimidator(player2.getId());

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void targetOpponentMayDiscardInsteadOfLosingLife() {
        harness.setHand(player2, List.of(new Forest()));
        castIntimidator(player2.getId());

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void offspringCreatesOneOneTokenCopyAndItsEtbAlsoTriggers() {
        harness.setHand(player1, List.of(new ThornplateIntimidator()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castKickedCreature(player1, 0, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ThornplateIntimidator()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentMayLoseLifeEvenWhenDiscardAndSacrificeAreAvailable() {
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new ThornplateIntimidator());

        castIntimidator(player2.getId());
        harness.handleListChoice(player2, "Lose 3 life");

        harness.assertLife(player2, 17);
        harness.assertInHand(player2, "Forest");
        harness.assertOnBattlefield(player2, "Thornplate Intimidator");
    }

    @Test
    void landsCannotBeSacrificedToAvoidLifeLoss() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());

        castIntimidator(player2.getId());

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void unpaidOffspringDoesNotCreateAToken() {
        harness.setHand(player2, List.of());

        castIntimidator(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertLife(player2, 17);
    }

    @Test
    void offspringAndLifeLossAreSeparateTriggeredAbilities() {
        harness.setHand(player1, List.of(new ThornplateIntimidator()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castKickedCreature(player1, 0, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void payingOffspringDoesNotMakeTheSpellKicked() {
        ThornplateIntimidator intimidator = new ThornplateIntimidator();
        harness.setHand(player1, List.of(intimidator));
        harness.setHand(player2, List.of(new ErtaisTrickery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, intimidator.getId());

        harness.assertNotInGraveyard(player1, "Thornplate Intimidator");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thornplate Intimidator");
    }

    private void castIntimidator(UUID targetId) {
        harness.setHand(player1, List.of(new ThornplateIntimidator()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();
    }
}
