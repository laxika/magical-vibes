package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunspineLynx.class, AngelOfMercy.class, Forest.class, Wasteland.class})
class SunspineLynxTest extends BaseCardTest {

    @Test
    @DisplayName("ETB damage counts each player's nonbasic lands separately and cannot be prevented")
    void entersAndDealsDamageBasedOnEachPlayersNonbasicLands() {
        harness.addToBattlefield(player1, new Wasteland());
        harness.addToBattlefield(player1, new Wasteland());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Wasteland());
        harness.addToBattlefield(player2, new Forest());
        gd.playerDamagePreventionShields.put(player1.getId(), 10);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        harness.setHand(player1, List.of(new SunspineLynx()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Players cannot gain life while Sunspine Lynx is on the battlefield")
    void playersCannotGainLife() {
        harness.addToBattlefield(player1, new SunspineLynx());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sunspine Lynx's static restrictions end when it leaves the battlefield")
    void staticRestrictionsEndWhenItLeaves() {
        harness.addToBattlefield(player1, new SunspineLynx());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
    }
}
