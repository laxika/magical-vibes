package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HussarPatrol;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Slitherwisp.class, AngelsMercy.class, Forest.class, HussarPatrol.class})
class SlitherwispTest extends BaseCardTest {

    @Test
    @DisplayName("Casting another spell with flash draws a card and makes each opponent lose 1 life")
    void flashSpellTriggersDrawAndLifeLoss() {
        harness.addToBattlefield(player1, new Slitherwisp());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HussarPatrol()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a spell without flash does not trigger Slitherwisp")
    void nonFlashSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Slitherwisp());
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }
}
