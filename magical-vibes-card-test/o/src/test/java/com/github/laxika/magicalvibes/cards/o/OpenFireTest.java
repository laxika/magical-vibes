package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.cards.w.WallOfForgottenPharaohs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpenFire.class, HillGiant.class, NicolBolasGodPharaoh.class, WallOfForgottenPharaohs.class})
class OpenFireTest extends BaseCardTest {

    @Test
    @DisplayName("Open Fire deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Open Fire deals 3 damage to target creature, destroying a 3/3")
    void deals3DamageToCreatureDestroysIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Open Fire can target its controller")
    void canDamageItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Open Fire");
    }

    @Test
    @DisplayName("Open Fire removes three loyalty counters from a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        target.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Nicol Bolas, God-Pharaoh");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Open Fire marks three damage on a surviving artifact creature")
    void damagesSurvivingArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfForgottenPharaohs());
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Forgotten Pharaohs");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertNotInGraveyard(player2, "Wall of Forgotten Pharaohs");
    }
}
