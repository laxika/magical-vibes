package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.ObliteratingBolt;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalPilgrim;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasCommand.class, ArgothianSprite.class, ObliteratingBolt.class,
        TeferiTemporalPilgrim.class, Forest.class, Mountain.class})
class MishrasCommandTest extends BaseCardTest {

    @Test
    void discardAndCreatureDamageModesResolveWithPaidX() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player2, List.of(new ObliteratingBolt(), new ObliteratingBolt(), new Forest()));
        harness.setLibrary(player2, List.of(new Mountain(), new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 1}, 2,
                List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player2, 2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void planeswalkerDamageAndCreaturePumpModesResolveWithHaste() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TeferiTemporalPilgrim());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{2, 3}, 3,
                List.of(planeswalker.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    void planeswalkerModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(player1, 0, 2,
                new int[]{2, 3}, 1, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetedPlayerCanDeclineDiscardingWithoutSkippingCreaturePump() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Forest retained = new Forest();
        Mountain undrawn = new Mountain();
        harness.setHand(player2, List.of(retained));
        harness.setLibrary(player2, List.of(undrawn));
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 2,
                List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void controllerCanDiscardFewerThanXAndPumpStillUsesPaidX() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Forest discarded = new Forest();
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new MishrasCommand(), discarded));
        harness.setLibrary(player1, List.of(drawn, new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 3,
                List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 2))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void zeroXStillGrantsHasteWithoutDiscardingOrDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Forest retained = new Forest();
        Mountain undrawn = new Mountain();
        harness.setHand(player2, List.of(retained));
        harness.setLibrary(player2, List.of(undrawn));
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 3}, 0,
                List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(undrawn);
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void sameCreatureCanBeDamagedAndPumpedAndBonusesExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{1, 3}, 1,
                List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void emptyHandDoesNotPreventOtherModeFromResolving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{0, 1}, 1,
                List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void creatureDamageAndPumpApplyOnlyToTheirRespectiveTargets() {
        Permanent damaged = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent pumped = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new MishrasCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorceryWithModesForX(player1, 0, 2, new int[]{1, 3}, 1,
                List.of(damaged.getId(), pumped.getId()));
        harness.passBothPriorities();

        assertThat(damaged.getMarkedDamage()).isEqualTo(1);
        assertThat(damaged.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, damaged, Keyword.HASTE)).isFalse();
        assertThat(pumped.getMarkedDamage()).isZero();
        assertThat(pumped.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, pumped, Keyword.HASTE)).isTrue();
    }
}
