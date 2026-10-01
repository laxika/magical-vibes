package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalamityOfCinders.class, ColossalDreadmaw.class, GrizzlyBears.class})
class CalamityOfCindersTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps a creature and the spell damages only untapped creatures")
    void convokeAndUntappedCreatureDamage() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new CalamityOfCinders()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreature.getId()));
        harness.passBothPriorities();

        assertThat(convokeCreature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(convokeCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(untappedCreature)
                .contains(tappedCreature);
    }
}
