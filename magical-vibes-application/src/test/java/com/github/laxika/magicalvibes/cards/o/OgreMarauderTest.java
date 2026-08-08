package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OgreMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player sacrifices a creature — the Ogre stays blockable")
    void sacrificeKeepsTheOgreBlockable() {
        Permanent ogre = addCreatureReady(player1, new OgreMarauder());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(ogre.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Defending player declines — the Ogre can't be blocked and its damage gets through")
    void declineMakesTheOgreUnblockable() {
        Permanent ogre = addCreatureReady(player1, new OgreMarauder());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(ogre.isCantBeBlocked()).isTrue();

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isBlockedThisTurn()).isFalse();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("With no creatures to sacrifice the Ogre simply becomes unblockable")
    void noCreaturesMeansNoPromptAndUnblockable() {
        Permanent ogre = addCreatureReady(player1, new OgreMarauder());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(ogre.isCantBeBlocked()).isTrue();
    }
}
