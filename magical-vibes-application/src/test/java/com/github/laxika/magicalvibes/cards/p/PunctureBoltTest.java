package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class PunctureBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and puts a -1/-1 counter on the target creature")
    void dealsDamageAndPutsCounter() {
        harness.addToBattlefield(player2, new HillGiant()); // 3/3
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent giant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hill Giant"))
                .findFirst().orElseThrow();
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(giant.getEffectivePower()).isEqualTo(2);
        assertThat(giant.getEffectiveToughness()).isEqualTo(2);
        assertThat(giant.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills a 2/2: -1/-1 counter plus 1 damage is lethal")
    void killsTwoTwo() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 -> 1/1 with 1 damage
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID spellbookId = harness.getPermanentId(player2, "Spellbook");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, spellbookId))
                .isInstanceOf(IllegalStateException.class);
    }
}
