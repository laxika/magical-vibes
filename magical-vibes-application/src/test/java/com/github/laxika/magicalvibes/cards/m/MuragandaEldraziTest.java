package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuragandaEldrazi.class, LlanowarElves.class, GrizzlyBears.class, Mountain.class})
class MuragandaEldraziTest extends BaseCardTest {

    @Test
    void putsPrimevalCounterOnTargetCreatureAndRemovesItsAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MuragandaEldrazi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PRIMEVAL)).isEqualTo(1);
        assertThat(gqs.hasLostAllAbilities(gd, target)).isTrue();
        assertThat(gqs.canActivateManaAbility(gd, target)).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new MuragandaEldrazi()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
