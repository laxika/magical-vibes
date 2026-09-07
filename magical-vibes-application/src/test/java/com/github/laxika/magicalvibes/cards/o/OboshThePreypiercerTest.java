package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OboshThePreypiercer.class, GrayOgre.class, GrizzlyBears.class, Incinerate.class,
        SerraAngel.class, Shock.class})
class OboshThePreypiercerTest extends BaseCardTest {

    @Test
    void doublesDamageFromOddManaValueSpell() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotDoubleDamageFromEvenManaValueSpell() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void doublesOddManaValueSpellDamageToPermanent() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    void doublesCombatDamageFromOddManaValueCreature() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        addCreatureReady(player1, new GrayOgre());

        declareAttackers(player1, List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void doesNotDoubleCombatDamageFromEvenManaValueCreature() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void onlyDoublesDamageFromSourcesControlledByItsController() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
