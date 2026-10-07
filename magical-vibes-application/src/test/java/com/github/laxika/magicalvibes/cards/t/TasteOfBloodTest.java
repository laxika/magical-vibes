package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.cards.p.PersonalSanctuary;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TasteOfBlood.class, ChandraTheFirebrand.class, PersonalSanctuary.class, RuneclawBear.class})
class TasteOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the target player and controller gains 1 life")
    void dealsDamageAndGainsLife() {
        int targetBefore = gd.getLife(player2.getId());
        int controllerBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new TasteOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, targetBefore - 1);
        harness.assertLife(player1, controllerBefore + 1);
    }

    @Test
    @DisplayName("Can target its controller at one life without causing a game loss")
    void canTargetControllerAtOneLife() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new TasteOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 1);
        assertThat(gd.gameResult).isNull();
        harness.assertInGraveyard(player1, "Taste of Blood");
    }

    @Test
    @DisplayName("Damages a planeswalker and gains life without damaging its controller")
    void damagesPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TasteOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when the sole target leaves the battlefield")
    void doesNotGainLifeWhenTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TasteOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Taste of Blood");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gains one life even when its damage is prevented")
    void gainsLifeWhenDamagePrevented() {
        harness.addToBattlefield(player1, new PersonalSanctuary());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TasteOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cannot target a creature that is not a planeswalker")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TasteOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
