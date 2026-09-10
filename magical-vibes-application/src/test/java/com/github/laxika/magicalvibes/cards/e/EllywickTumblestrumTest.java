package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AcererakTheArchlich;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EllywickTumblestrum.class, AcererakTheArchlich.class, Forest.class,
        GrizzlyBears.class, Island.class})
class EllywickTumblestrumTest extends BaseCardTest {

    @Test
    @DisplayName("+1 ventures into a dungeon")
    void plusOneVenture() {
        addReadyEllywick(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("-2 puts a creature into hand and gains 3 life for a legendary creature")
    void minusTwoLegendaryCreatureGainsLife() {
        addReadyEllywick(player1, 4);
        AcererakTheArchlich legendaryCreature = new AcererakTheArchlich();
        setTopCards(List.of(new Forest(), new Island(), new GrizzlyBears(),
                new Forest(), new Island(), legendaryCreature));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("-2 does not gain life for a nonlegendary creature")
    void minusTwoNonlegendaryCreatureDoesNotGainLife() {
        addReadyEllywick(player1, 4);
        GrizzlyBears creature = new GrizzlyBears();
        setTopCards(List.of(new Forest(), new Island(), new Forest(),
                new Island(), new Forest(), creature));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("-7 grants trample, haste, and a dynamic boost for completed dungeons")
    void minusSevenCreatesDynamicEmblem() {
        addReadyEllywick(player1, 7);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        gd.recordCompletedDungeon(player1.getId(), Dungeon.LOST_MINE_OF_PHANDELVER);
        gd.recordCompletedDungeon(player1.getId(), Dungeon.TOMB_OF_ANNIHILATION);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();

        gd.recordCompletedDungeon(player1.getId(), Dungeon.DUNGEON_OF_THE_MAD_MAGE);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(8);
    }

    private void setTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private Permanent addReadyEllywick(Player player, int loyalty) {
        Permanent perm = new Permanent(new EllywickTumblestrum());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
