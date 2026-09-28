package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BandingSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OddricLunarMarquis.class, BandingSliver.class, GrizzlyBears.class,
        LlanowarElves.class, MistIntruder.class})
class OddricLunarMarquisTest extends BaseCardTest {

    private OddricLunarMarquis oddric() {
        OddricLunarMarquis oddric = new OddricLunarMarquis();
        oddric.setName("Oddric, Lunar Marquis");
        oddric.setType(CardType.CREATURE);
        oddric.setPower(3);
        oddric.setToughness(3);
        return oddric;
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Shares the listed keywords at the beginning of each combat")
    void sharesKeywords() {
        Permanent oddric = harness.addToBattlefieldAndReturn(player1, oddric());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new BandingSliver());
        Permanent ingestCreature = harness.addToBattlefieldAndReturn(player1, new MistIntruder());
        ingestCreature.getGrantedKeywords().add(Keyword.TANTRUM);

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, oddric, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TANTRUM)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INGEST)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TANTRUM)).isTrue();
    }

    @Test
    @DisplayName("Shares the sacrifice-for-colorless ability when a creature has a mana ability")
    void sharesColorlessSacrificeAbility() {
        harness.addToBattlefield(player1, oddric());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToCombatAndResolve(player1);

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        harness.activateAbility(player1, bearsIndex, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(com.github.laxika.magicalvibes.model.ManaColor.COLORLESS))
                .isEqualTo(1);
    }
}
