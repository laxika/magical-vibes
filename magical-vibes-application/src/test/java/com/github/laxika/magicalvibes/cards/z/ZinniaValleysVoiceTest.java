package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZinniaValleysVoice.class, LlanowarElves.class, GrizzlyBears.class})
class ZinniaValleysVoiceTest extends BaseCardTest {

    @Test
    void getsPlusOnePowerForEachOtherControlledCreatureWithBasePowerOne() {
        Permanent zinnia = harness.addToBattlefieldAndReturn(player1, new ZinniaValleysVoice());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        assertThat(zinnia.getEffectivePower()).isEqualTo(3);
        assertThat(zinnia.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void grantsOffspringToCreatureSpells() {
        harness.addToBattlefield(player1, new ZinniaValleysVoice());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1)
                .allMatch(token -> token.getEffectivePower() == 1
                        && token.getEffectiveToughness() == 1);
    }
}
