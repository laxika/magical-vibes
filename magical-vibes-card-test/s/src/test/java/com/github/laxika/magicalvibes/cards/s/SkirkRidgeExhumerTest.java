package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirkRidgeExhumer.class, Forest.class, GrizzlyBears.class, Shock.class})
class SkirkRidgeExhumerTest extends BaseCardTest {

    @Test
    void activationDiscardsACardAndCreatesAFesteringGoblinToken() {
        Permanent exhumer = addReadyExhumer();
        Card discarded = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(exhumer.isTapped()).isTrue();
    }

    @Test
    void createdTokenHasTheFesteringGoblinCharacteristics() {
        addReadyExhumer();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Festering Goblin");
        assertThat(token.getCard().getName()).isEqualTo("Festering Goblin");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.GOBLIN);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void festeringGoblinShrinksATargetCreatureWhenItDies() {
        addReadyExhumer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Festering Goblin");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, token.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void festeringGoblinCanShrinkACreatureItsControllerControlsWhenItDies() {
        addReadyExhumer();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Festering Goblin");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, token.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void festeringGoblinShrinksACreatureUntilEndOfTurn() {
        addReadyExhumer();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Festering Goblin");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, token.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent exhumer = addReadyExhumer();
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(exhumer.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Festering Goblin")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent exhumer = harness.addToBattlefieldAndReturn(player1, new SkirkRidgeExhumer());
        exhumer.setSummoningSick(true);
        Card discard = new Forest();
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(exhumer.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dyingTokenMustShrinkTheExhumerWhenItIsTheOnlyRemainingCreature() {
        Permanent exhumer = addReadyExhumer();
        harness.setHand(player1, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Festering Goblin");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, token.getId());
        harness.handlePermanentChosen(player1, exhumer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skirk Ridge Exhumer");
        harness.assertInGraveyard(player1, "Skirk Ridge Exhumer");
        assertThat(countPermanents(player1, "Festering Goblin")).isZero();
    }

    private Permanent addReadyExhumer() {
        return addCreatureReady(player1, new SkirkRidgeExhumer());
    }
}
