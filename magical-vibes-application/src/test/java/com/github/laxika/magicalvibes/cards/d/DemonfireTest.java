package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Demonfire.class, Cancel.class, GrizzlyBears.class, HealingSalve.class})
class DemonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to any target")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A non-hellbent Demonfire can be countered")
    void nonHellbentCanBeCountered() {
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, demonfire.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Demonfire");
    }

    @Test
    @DisplayName("A hellbent Demonfire can't be countered")
    void hellbentCannotBeCountered() {
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, demonfire.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Non-hellbent damage can be prevented")
    void nonHellbentDamageCanBePrevented() {
        harness.setHand(player1, List.of(new Demonfire(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Hellbent damage can't be prevented")
    void hellbentDamageCannotBePrevented() {
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A creature dealt lethal Demonfire damage is exiled")
    void lethalDamageExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
