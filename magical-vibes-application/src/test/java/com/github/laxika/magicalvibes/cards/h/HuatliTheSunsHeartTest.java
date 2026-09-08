package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BelligerentBrontodon;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuatliTheSunsHeart.class, GoblinPiker.class, GiantSpider.class, BelligerentBrontodon.class})
class HuatliTheSunsHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures assign combat damage using toughness")
    void ownCreaturesUseToughnessForCombatDamage() {
        addReadyHuatli(player1, 3);
        Permanent ownPiker = addReadyCreature(player1, new GoblinPiker());
        Permanent opponentPiker = addReadyCreature(player2, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, ownPiker)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentPiker)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 gains life equal to the greatest toughness among your creatures")
    void minusThreeGainsGreatestControlledToughness() {
        Permanent huatli = addReadyHuatli(player1, 3);
        addReadyCreature(player1, new GoblinPiker());
        addReadyCreature(player1, new GiantSpider());
        addReadyCreature(player2, new BelligerentBrontodon());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(huatli.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    private Permanent addReadyHuatli(Player player, int loyalty) {
        Permanent perm = new Permanent(new HuatliTheSunsHeart());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
