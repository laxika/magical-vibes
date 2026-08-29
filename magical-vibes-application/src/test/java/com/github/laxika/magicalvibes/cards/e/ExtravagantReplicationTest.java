package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExtravagantReplicationTest extends BaseCardTest {

    @Test
    @DisplayName("Your upkeep presents a target for another nonland permanent you control")
    void upkeepPresentsTargetSelection() {
        addReplicationReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving the upkeep trigger creates a token copy of the target")
    void createsTokenCopy() {
        addReplicationReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken()
                        && p.getCard().getName().equals("Grizzly Bears")
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("The trigger has no legal target when only lands or opponents' permanents remain")
    void rejectsInvalidTargets() {
        addReplicationReady(player1);
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReplicationReady(Player player) {
        Permanent permanent = new Permanent(new ExtravagantReplication());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
