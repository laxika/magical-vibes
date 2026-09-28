package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoronationOfTheWilds;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeeperOfTheCrown.class, CoronationOfTheWilds.class, GrizzlyBears.class, Forest.class})
class KeeperOfTheCrownTest extends BaseCardTest {

    @Test
    void adventureMakesCreatureLegendaryNobleGrantsManaAndDraws() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new KeeperOfTheCrown());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(false);

        harness.setHand(player1, List.of(new CoronationOfTheWilds()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.NOBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);

        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        harness.activateAbility(player1, targetIndex, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.getLegendarySourceManaTotal()).isEqualTo(1);
        assertThat(new ManaCost("{2}{L}").canPay(mana, 0)).isFalse();
        mana.add(ManaColor.COLORLESS, 2);
        assertThat(new ManaCost("{2}{L}").canPay(mana, 0)).isTrue();
        new ManaCost("{2}{L}").pay(mana, 0);
        assertThat(mana.getTotalAllMana()).isZero();
        assertThat(keeper).isNotNull();
    }
}
