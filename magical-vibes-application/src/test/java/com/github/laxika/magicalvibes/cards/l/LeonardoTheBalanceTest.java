package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeonardoTheBalance.class, RaiseTheAlarm.class, GrizzlyBears.class})
class LeonardoTheBalanceTest extends BaseCardTest {

    @Test
    void mayPutCountersOnAllControlledCreaturesOnlyOnceEachTurnWhenTokensEnter() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoTheBalance());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RaiseTheAlarm(), new RaiseTheAlarm()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addRaiseTheAlarmMana(player1);

        castRaiseTheAlarm(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, leonardo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leonardo)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);

        castRaiseTheAlarm(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, leonardo)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
    }

    @Test
    void activatedAbilityGrantsKeywordsUntilEndOfTurn() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoTheBalance());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        addMana(player1, ManaColor.WHITE, 1);
        addMana(player1, ManaColor.BLUE, 1);
        addMana(player1, ManaColor.BLACK, 1);
        addMana(player1, ManaColor.RED, 1);
        addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, leonardo), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.MENACE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
    }

    private void castRaiseTheAlarm(Player player) {
        harness.castInstant(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addRaiseTheAlarmMana(Player player) {
        addMana(player, ManaColor.WHITE, 2);
        addMana(player, ManaColor.COLORLESS, 2);
    }

    private void addMana(Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
