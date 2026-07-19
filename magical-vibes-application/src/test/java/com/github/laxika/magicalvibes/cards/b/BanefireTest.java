package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BanefireTest extends BaseCardTest {

    // ===== Deals X damage to any target =====

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new Banefire()));
        harness.addMana(player1, ManaColor.RED, 4); // X=3 + {R}
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals X damage to target creature, destroying it")
    void dealsXDamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Banefire()));
        harness.addMana(player1, ManaColor.RED, 3); // X=2 + {R}

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== "If X is 5 or more, this spell can't be countered" =====

    @Test
    @DisplayName("With X below 5 Banefire can be countered")
    void belowFiveCanBeCountered() {
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 4); // X=3 + {R}
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, banefire.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Countered: no damage dealt and Banefire is in its owner's graveyard.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Banefire"));
    }

    @Test
    @DisplayName("With X of 5 or more Banefire can't be countered")
    void fiveOrMoreCannotBeCountered() {
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 6); // X=5 + {R}
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, banefire.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Cancel resolved but couldn't counter — Banefire still dealt its 5 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    // ===== "If X is 5 or more, the damage can't be prevented" =====

    @Test
    @DisplayName("With X below 5 the damage can be prevented")
    void belowFiveDamageCanBePrevented() {
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 4); // X=3 + {R}
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passPriority(player1);
        // Player2 shields themselves for the next 3 damage (HealingSalve mode 1) in response.
        harness.castInstant(player2, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // All 3 damage prevented — player2's life is untouched.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With X of 5 or more the damage can't be prevented")
    void fiveOrMoreDamageCannotBePrevented() {
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 6); // X=5 + {R}
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passPriority(player1);
        // Player2 tries to shield themselves for the next 3 damage — but the damage can't be prevented.
        harness.castInstant(player2, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Prevention shield is bypassed — player2 takes the full 5 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
