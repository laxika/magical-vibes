package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AllSeeingArbiter.class, Censor.class, Forest.class, GrizzlyBears.class, Shock.class})
class AllSeeingArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws two cards, then discards a card")
    void entersDrawsTwoThenDiscards() {
        harness.setHand(player1, List.of(new AllSeeingArbiter()));
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking draws two cards, then discards a card")
    void attacksDrawsTwoThenDiscards() {
        addCreatureReady(player1, new AllSeeingArbiter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discarding a card gives an opponent creature -X/-0 for distinct graveyard mana values")
    void discardShrinksOpponentCreatureByDistinctManaValues() {
        harness.addToBattlefield(player1, new AllSeeingArbiter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The discard debuff remains through the turn and expires on the controller's next turn")
    void discardDebuffExpiresOnNextTurn() {
        harness.addToBattlefield(player1, new AllSeeingArbiter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }


    @Test
    @CardUsed({AllSeeingArbiter.class, Forest.class})
    @DisplayName("The attack discard counts lands as one mana value and ignores the opponent's graveyard")
    void attackDiscardCountsLandManaValue() {
        addCreatureReady(player1, new AllSeeingArbiter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AllSeeingArbiter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new AllSeeingArbiter()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @CardUsed({AllSeeingArbiter.class, Forest.class})
    @DisplayName("X is evaluated at resolution and stays fixed afterward")
    void manaValuesAreCountedAtResolution() {
        addCreatureReady(player1, new AllSeeingArbiter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AllSeeingArbiter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new Forest(), new AllSeeingArbiter()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @CardUsed({AllSeeingArbiter.class, Forest.class})
    @DisplayName("The discard trigger cannot target the controller's own creature")
    void discardCannotTargetOwnCreature() {
        Permanent source = addCreatureReady(player1, new AllSeeingArbiter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AllSeeingArbiter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    @CardUsed({AllSeeingArbiter.class, Forest.class})
    @DisplayName("A target that changes to the ability controller is illegal at resolution")
    void targetMustRemainUnderOpponentControl() {
        addCreatureReady(player1, new AllSeeingArbiter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AllSeeingArbiter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }
}
