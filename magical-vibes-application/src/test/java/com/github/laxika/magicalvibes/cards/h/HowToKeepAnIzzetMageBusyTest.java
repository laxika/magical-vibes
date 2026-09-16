package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowToKeepAnIzzetMageBusy.class, Counterspell.class})
class HowToKeepAnIzzetMageBusyTest extends BaseCardTest {

    @Test
    void returnsToOwnersHandWhenItResolves() {
        HowToKeepAnIzzetMageBusy spell = new HowToKeepAnIzzetMageBusy();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void goesToGraveyardWhenCountered() {
        HowToKeepAnIzzetMageBusy spell = new HowToKeepAnIzzetMageBusy();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }
}
