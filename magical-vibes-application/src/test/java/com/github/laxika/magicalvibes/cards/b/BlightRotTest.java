package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlightRotTest extends BaseCardTest {

    @Test
    @DisplayName("Puts four -1/-1 counters on target creature")
    void putsFourMinusOneMinusOneCountersOnTargetCreature() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new BlightRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Colossal Dreadmaw");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Colossal Dreadmaw");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kills a creature whose toughness is reduced to zero")
    void killsCreatureWhenCountersReduceToughnessToZero() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BlightRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new BlightRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
