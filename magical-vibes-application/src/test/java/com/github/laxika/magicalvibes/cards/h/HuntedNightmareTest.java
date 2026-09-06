package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntedNightmare.class, GrizzlyBears.class, GiantSpider.class})
class HuntedNightmareTest extends BaseCardTest {

    @Test
    void targetOpponentChoosesCreatureForDeathtouchCounter() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        castHuntedNightmare(player2.getId());

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(bears.getId(), spider.getId());
        assertThat(choice.validIds()).doesNotContain(ownCreature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(spider.getId()));

        assertThat(spider.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.DEATHTOUCH)).isTrue();
        assertThat(bears.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void targetOpponentWithOneCreatureGetsCounterWithoutChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castHuntedNightmare(player2.getId());

        GameData gd = harness.getGameData();
        assertThat(creature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void targetOpponentWithNoCreaturesPutsNoCounter() {
        castHuntedNightmare(player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new HuntedNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHuntedNightmare(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new HuntedNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, List.of(targetPlayerId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
