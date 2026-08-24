package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderheadSquadron.class, GrizzlyBears.class})
class ThunderheadSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke lets a creature spell use a creature to pay generic mana")
    void convokePaysGenericMana() {
        Permanent convokingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThunderheadSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokingCreature.getId()));

        assertThat(convokingCreature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ThunderheadSquadron);
    }

    @Test
    @DisplayName("Has flying")
    void hasFlying() {
        Permanent squadron = harness.addToBattlefieldAndReturn(player1, new ThunderheadSquadron());

        assertThat(gqs.hasKeyword(gd, squadron, Keyword.FLYING)).isTrue();
    }
}
