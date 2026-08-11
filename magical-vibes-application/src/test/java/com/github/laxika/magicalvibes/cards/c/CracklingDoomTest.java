package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CracklingDoomTest extends BaseCardTest {

    private void addCracklingDoomMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void castCracklingDoom() {
        harness.setHand(player1, List.of(new CracklingDoom()));
        addCracklingDoomMana();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private List<UUID> permanentIds(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .map(Permanent::getId)
                .toList();
    }

    @Test
    @DisplayName("Deals 2 damage to each opponent and sacrifices that opponent's greatest-power creature")
    void damagesOpponentAndSacrificesGreatestPowerCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castCracklingDoom();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Allows the opponent to choose among creatures tied for greatest power")
    void opponentChoosesAmongGreatestPowerTie() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());

        castCracklingDoom();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        List<UUID> hillGiantIds = permanentIds(player2, "Hill Giant");
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(hillGiantIds);

        harness.handleMultiplePermanentsChosen(player2, List.of(hillGiantIds.get(0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
