package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutationalAdvantage.class, Forest.class, GrizzlyBears.class, Pyroclasm.class})
class MutationalAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Protects your countered permanents and proliferates")
    void protectsCounteredPermanentsAndProliferates() {
        Permanent counteredCreature = addCounteredBear(player1);
        Permanent counteredLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        counteredLand.setCounterCount(CounterType.CHARGE, 1);
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = addCounteredBear(player2);

        cast();

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredLand, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds())
                .contains(counteredCreature.getId(), counteredLand.getId(), opponentCreature.getId())
                .doesNotContain(uncounteredCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(counteredCreature.getId(), counteredLand.getId()));

        assertThat(counteredCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(counteredLand.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents damage to countered permanents you control")
    void preventsDamageToCounteredPermanentsYouControl() {
        Permanent protectedCreature = addCounteredBear(player1);
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(protectedCreature.getId()));

        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(protectedCreature)
                .doesNotContain(unprotectedCreature);
    }

    @Test
    @DisplayName("Protection keywords expire at end of turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent protectedCreature = addCounteredBear(player1);

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addCounteredBear(Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.setCounterCount(CounterType.CHARGE, 1);
        return bear;
    }

    private void cast() {
        harness.setHand(player1, List.of(new MutationalAdvantage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
