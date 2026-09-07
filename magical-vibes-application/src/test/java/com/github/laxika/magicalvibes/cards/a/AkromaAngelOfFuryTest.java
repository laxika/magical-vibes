package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkromaAngelOfFury.class, Cancel.class, SwordsToPlowshares.class, Unsummon.class})
class AkromaAngelOfFuryTest extends BaseCardTest {

    @Test
    void cannotBeCounteredByCancel() {
        AkromaAngelOfFury akroma = new AkromaAngelOfFury();
        harness.setHand(player1, List.of(akroma));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, akroma.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Akroma, Angel of Fury");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void protectionFromWhiteAndBluePreventsTargeting() {
        Permanent akroma = harness.addToBattlefieldAndReturn(player2, new AkromaAngelOfFury());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, akroma.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, akroma.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redAbilityBoostsPowerUntilEndOfTurn() {
        Permanent akroma = harness.addToBattlefieldAndReturn(player1, new AkromaAngelOfFury());
        int basePower = gqs.getEffectivePower(gd, akroma);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, akroma)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, akroma)).isEqualTo(basePower);
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new AkromaAngelOfFury()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent akroma = findPermanent(player1, "Akroma, Angel of Fury");
        assertThat(akroma.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(akroma));
        harness.passBothPriorities();

        assertThat(akroma.isFaceDown()).isFalse();
    }
}
