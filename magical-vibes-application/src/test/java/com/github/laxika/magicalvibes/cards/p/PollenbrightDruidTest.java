package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({PollenbrightDruid.class, GrizzlyBears.class, Spellbook.class})
class PollenbrightDruidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode puts a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castDruid(0, bears.getId());
        resolveCreatureAndEtb();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB mode proliferates")
    void proliferates() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castDruid(1);
        resolveCreatureAndEtb();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counter mode rejects a noncreature target")
    void counterModeRejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new Spellbook());
        Permanent spellbook = gd.playerBattlefields.get(player2.getId()).getLast();

        assertThatThrownBy(() -> castDruid(0, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castDruid(int mode) {
        castDruid(mode, null);
    }

    private void castDruid(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PollenbrightDruid()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
