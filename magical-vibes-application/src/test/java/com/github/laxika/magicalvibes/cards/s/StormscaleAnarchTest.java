package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IzzetGuildmage;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormscaleAnarch.class, LlanowarElves.class, IzzetGuildmage.class})
class StormscaleAnarchTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWhenTheDiscardedCardIsNotMulticolored() {
        harness.addToBattlefield(player1, new StormscaleAnarch());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void dealsFourDamageWhenTheDiscardedCardIsMulticolored() {
        harness.addToBattlefield(player1, new StormscaleAnarch());
        harness.setHand(player1, List.of(new IzzetGuildmage()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Izzet Guildmage");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
