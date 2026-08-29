package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElendaTheDuskRoseTest extends BaseCardTest {

    @Test
    void createsLifelinkVampireTokensEqualToPowerWhenItDies() {
        harness.addToBattlefield(player1, new ElendaTheDuskRose());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killPermanent(player2, "Grizzly Bears");

        Permanent elenda = findPermanent(player1, "Elenda, the Dusk Rose");
        assertThat(elenda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elenda)).isEqualTo(2);

        killPermanent(player1, "Elenda, the Dusk Rose");

        List<Permanent> tokens = findPermanents(player1, "Vampire");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
    }

    private void killPermanent(Player controller, String name) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID permanentId = harness.getPermanentId(controller, name);
        harness.castInstant(player2, 0, permanentId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
