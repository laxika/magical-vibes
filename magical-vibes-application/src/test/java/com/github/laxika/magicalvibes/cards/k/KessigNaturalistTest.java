package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.cards.l.LordOfTheUlvenwald;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KessigNaturalist.class, LordOfTheUlvenwald.class, HowlpackWolf.class})
class KessigNaturalistTest extends BaseCardTest {

    @Test
    void frontFaceAttackAddsChosenPersistentMana() {
        addCreatureReady(player1, new KessigNaturalist());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Add {R}");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getPersistentMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    void backFaceAttackAddsChosenPersistentMana() {
        addCreatureReady(player1, new LordOfTheUlvenwald());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Add {G}");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.getPersistentMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    void backFaceBoostsOnlyOwnOtherWolvesAndWerewolves() {
        Permanent lord = addCreatureReady(player1, new LordOfTheUlvenwald());
        Permanent ownWolf = addCreatureReady(player1, new HowlpackWolf());
        Permanent opposingWolf = addCreatureReady(player2, new HowlpackWolf());

        assertThat(gqs.getEffectivePower(gd, ownWolf)).isEqualTo(gqs.getEffectivePower(gd, opposingWolf) + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownWolf)).isEqualTo(gqs.getEffectiveToughness(gd, opposingWolf) + 1);
        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(lord.getBasePower());
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(lord.getBaseToughness());
    }

    @Test
    void dayAndNightTransformTheFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent naturalist = harness.addToBattlefieldAndReturn(player1, new KessigNaturalist());

        gd.spellsCastLastTurn.clear();
        advanceToNextTurn(player1);
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(naturalist.isTransformed()).isTrue();
        assertThat(naturalist.getCard()).isInstanceOf(LordOfTheUlvenwald.class);

        gd.recordSpellCast(player1.getId(), new KessigNaturalist());
        gd.recordSpellCast(player1.getId(), new KessigNaturalist());
        advanceToNextTurn(player2);
        assertThat(naturalist.isTransformed()).isFalse();
        assertThat(naturalist.getCard()).isInstanceOf(KessigNaturalist.class);
    }

    private void advanceToNextTurn(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
