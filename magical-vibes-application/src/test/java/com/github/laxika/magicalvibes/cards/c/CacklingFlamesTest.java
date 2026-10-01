package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DemonsJester;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacklingFlames.class, DemonsJester.class})
class CacklingFlamesTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithCardsInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CacklingFlames(), new CacklingFlames()));
        addMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void dealsFiveDamageWithAnEmptyHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CacklingFlames()));
        addMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void dealsFiveDamageToTargetCreature() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CacklingFlames()));
        harness.setHand(player2, List.of(new CacklingFlames()));
        harness.addToBattlefield(player2, new DemonsJester());
        addMana();

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Demon's Jester"));

        harness.assertInGraveyard(player2, "Demon's Jester");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void checksHellbentWhenTheSpellResolves() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CacklingFlames()));
        addMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player1, List.of(new CacklingFlames()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
