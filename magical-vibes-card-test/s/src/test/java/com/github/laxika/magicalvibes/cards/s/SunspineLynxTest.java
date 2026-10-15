package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunspineLynx.class, AngelOfMercy.class, Forest.class, Wasteland.class, TurnToFrog.class})
class SunspineLynxTest extends BaseCardTest {
    @Test
    @DisplayName("Sunspine Lynx also prevents its controller from gaining life")
    void controllerCannotGainLife() {
        harness.addToBattlefield(player1, new SunspineLynx());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("ETB still resolves after Lynx leaves, but its damage can then be prevented")
    void triggerResolvesWithPreventionAfterSourceLeaves() {
        harness.addToBattlefield(player2, new Wasteland());
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new SunspineLynx()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Players can gain life after Sunspine Lynx loses all abilities")
    void lifeGainAllowedAfterLosingAbilities() {
        var lynx = harness.addToBattlefieldAndReturn(player1, new SunspineLynx());
        harness.setHand(player1, List.of(new TurnToFrog(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, lynx.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("ETB counts nonbasic lands when the trigger resolves")
    void countsLandsAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SunspineLynx()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new Wasteland());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

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
