package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BronzeHorseTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from a spell that targets it while you control another creature")
    void preventsDamageFromTargetingSpellWithAnotherCreature() {
        Permanent horse = harness.addToBattlefieldAndReturn(player1, new BronzeHorse());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, horse.getId());
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent targeted spell damage when you control no other creature")
    void doesNotPreventDamageWithoutAnotherCreature() {
        Permanent horse = harness.addToBattlefieldAndReturn(player1, new BronzeHorse());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, horse.getId());
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not prevent damage from a spell that does not target it")
    void doesNotPreventDamageFromNontargetingSpell() {
        Permanent horse = harness.addToBattlefieldAndReturn(player1, new BronzeHorse());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Pyroclasm()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(horse.getMarkedDamage()).isEqualTo(2);
    }
}
