package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SigardaHostOfHerons;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreMarauder.class, GrizzlyBears.class, SigardaHostOfHerons.class})
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

    @Test
    @DisplayName("Defending player chooses which creature to sacrifice")
    void defendingPlayerChoosesCreatureToSacrifice() {
        Permanent ogre = addCreatureReady(player1, new OgreMarauder());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        assertThat(ogre.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Sigarda prevents an opponent's trigger from forcing a sacrifice")
    void sacrificeProtectionPreventsOpponentSacrifice() {
        Permanent ogre = addCreatureReady(player1, new OgreMarauder());
        Permanent sigarda = addCreatureReady(player2, new SigardaHostOfHerons());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(sigarda);
        assertThat(ogre.isCantBeBlocked()).isTrue();
    }
}
