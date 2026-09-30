package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TheGitrogMonster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MythweaverPoq.class, Forest.class, Plains.class, TheGitrogMonster.class})
class MythweaverPoqTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualControlledLands() {
        Permanent poq = addCreatureReady(player1, new MythweaverPoq());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, poq)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, poq)).isEqualTo(2);
    }

    @Test
    void landfallConjuresNonTokenDuplicateOfEnteringLand() {
        addCreatureReady(player1, new MythweaverPoq());
        harness.addToBattlefield(player1, new Forest());
        Card enteringLand = new Plains();
        harness.setHand(player1, List.of(enteringLand));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        List<Permanent> lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .toList();
        assertThat(lands).hasSize(3);
        assertThat(lands).allMatch(permanent -> !permanent.getCard().isToken());
        assertThat(lands).extracting(permanent -> permanent.getCard().getId())
                .contains(enteringLand.getId())
                .doesNotHaveDuplicates();
    }

    @Test
    void landfallTriggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new MythweaverPoq());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.setHand(player1, List.of(new Plains(), new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        long landCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
        assertThat(landCount).isEqualTo(4);
    }
}
