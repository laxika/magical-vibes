package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DarksteelMutation;
import com.github.laxika.magicalvibes.cards.r.RemoteIsle;
import com.github.laxika.magicalvibes.cards.s.SharkTyphoon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaviNestWarden.class, Censor.class, GrizzlyBears.class, CounselOfTheSoratami.class,
        RemoteIsle.class, SharkTyphoon.class, DarksteelMutation.class})
class GaviNestWardenTest extends BaseCardTest {

    @Test
    @DisplayName("The first card cycled each turn costs no mana, but the second does")
    void firstCardCycledEachTurnIsFreeOnlyOnce() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        Censor first = new Censor();
        Censor second = new Censor();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getId())
                .contains(first.getId());
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Drawing the second card each turn creates a 2/2 red and white Dinosaur Cat")
    void secondDrawCreatesDinosaurCat() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Dinosaur Cat");
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.CAT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void cyclingBeforeGaviEntersUsesUpTheFirstCycle() {
        harness.setHand(player1, List.of(new RemoteIsle(), new RemoteIsle()));
        harness.setLibrary(player1, List.of(new GaviNestWarden(), new GaviNestWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new GaviNestWarden());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstCycleOnOpponentsTurnIsFreeAndOnlySecondDrawCreatesToken() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        harness.setHand(player1, List.of(new RemoteIsle(), new RemoteIsle(), new RemoteIsle()));
        harness.setLibrary(player1, List.of(new GaviNestWarden(), new GaviNestWarden(), new GaviNestWarden()));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dinosaur Cat")).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dinosaur Cat")).isEqualTo(1);

        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dinosaur Cat")).isEqualTo(1);
    }

    @Test
    void opponentsDrawsDoNotCreateTokensOrReceiveFreeCycling() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        harness.setHand(player2, List.of(new RemoteIsle(), new RemoteIsle()));
        harness.setLibrary(player2, List.of(new GaviNestWarden(), new GaviNestWarden()));
        harness.ensurePriority(player2);
        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();
        harness.ensurePriority(player2);
        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dinosaur Cat")).isZero();
        assertThat(countPermanents(player2, "Dinosaur Cat")).isZero();
    }

    @Test
    void firstDrawBeforeGaviEntersStillCountsForTokenTrigger() {
        harness.setHand(player1, List.of(new RemoteIsle(), new RemoteIsle()));
        harness.setLibrary(player1, List.of(new GaviNestWarden(), new GaviNestWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new GaviNestWarden());

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dinosaur Cat")).isEqualTo(1);
    }

    @Test
    void enteringAfterSecondDrawDoesNotTriggerOnThirdDraw() {
        harness.setHand(player1, List.of(new RemoteIsle(), new RemoteIsle(), new RemoteIsle()));
        harness.setLibrary(player1, List.of(new GaviNestWarden(), new GaviNestWarden(), new GaviNestWarden()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new GaviNestWarden());

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dinosaur Cat")).isZero();
    }

    @Test
    void freeCyclingAndSecondDrawTriggerResetOnTheNextTurn() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        harness.setHand(player1, List.of(new RemoteIsle(), new RemoteIsle(), new RemoteIsle(), new RemoteIsle()));
        harness.setLibrary(player1, List.of(new GaviNestWarden(), new GaviNestWarden(),
                new GaviNestWarden(), new GaviNestWarden()));
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dinosaur Cat")).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dinosaur Cat")).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dinosaur Cat")).isEqualTo(2);
    }

    @Test
    void freeCyclingCannotChoosePositiveX() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        harness.setHand(player1, List.of(new SharkTyphoon()));
        harness.setLibrary(player1, List.of(new GaviNestWarden()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 5))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAbilitiesDisablesFreeCycling() {
        Permanent gavi = harness.addToBattlefieldAndReturn(player1, new GaviNestWarden());
        harness.setHand(player1, List.of(new DarksteelMutation(), new RemoteIsle()));
        harness.setLibrary(player1, List.of(new GaviNestWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, gavi.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAbilitiesDisablesSecondDrawTrigger() {
        Permanent gavi = harness.addToBattlefieldAndReturn(player1, new GaviNestWarden());
        harness.setHand(player1, List.of(new DarksteelMutation(), new RemoteIsle(), new RemoteIsle()));
        harness.setLibrary(player1, List.of(new GaviNestWarden(), new GaviNestWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castEnchantment(player1, 0, gavi.getId());
        resolveAllTriggers();

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dinosaur Cat")).isZero();
    }
}
