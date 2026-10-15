package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpeditionChampion.class, GrizzlyBears.class})
class ExpeditionChampionTest extends BaseCardTest {

    @Test
    void hasBaseStatsWithoutAnotherWarrior() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    void getsPlusTwoPowerWithAnotherWarrior() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        harness.addToBattlefield(player1, createWarrior());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    @Test
    void opponentWarriorDoesNotCount() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        harness.addToBattlefield(player2, createWarrior());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
    }

    @Test
    void losesBonusWhenAnotherWarriorLeaves() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        harness.addToBattlefield(player1, createWarrior());

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.WARRIOR)
                        && !permanent.getCard().getName().equals("Expedition Champion"));

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
    }

    @Test
    void championsWithTheSameNameEnableEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void bonusDoesNotStackAndUpdatesAsWarriorsEnterAndLeave() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);

        Permanent second = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);

        Permanent third = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(third);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }

    private Card createWarrior() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.WARRIOR));
        return card;
    }
}
