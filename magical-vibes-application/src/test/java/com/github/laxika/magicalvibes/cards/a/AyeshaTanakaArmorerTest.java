package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlackManaBattery;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({AyeshaTanakaArmorer.class, BlackManaBattery.class, GrizzlyBears.class, MindStone.class,
        Ornithopter.class})
class AyeshaTanakaArmorerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts any number of eligible artifacts onto the battlefield tapped")
    void attackTriggerPutsEligibleArtifactsTapped() {
        addReadyAyesha();
        MindStone mindStone = new MindStone();
        Ornithopter ornithopter = new Ornithopter();
        BlackManaBattery tooExpensive = new BlackManaBattery();
        GrizzlyBears nonArtifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(mindStone, ornithopter, tooExpensive, nonArtifact));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(mindStone.getId(), ornithopter.getId());
        harness.handleMultipleCardsChosen(player1, List.of(mindStone.getId(), ornithopter.getId()));

        Permanent enteredMindStone = findPermanent(player1, "Mind Stone");
        Permanent enteredOrnithopter = findPermanent(player1, "Ornithopter");
        assertThat(enteredMindStone.isTapped()).isTrue();
        assertThat(enteredOrnithopter.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(tooExpensive, nonArtifact);
    }

    @Test
    @DisplayName("Ayesha can't be blocked when the defending player controls three artifacts")
    void cantBeBlockedWithThreeDefendingArtifacts() {
        Permanent ayesha = addReadyAyesha();
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new MindStone());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(ayesha)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Ayesha can be blocked when the defending player controls fewer than three artifacts")
    void canBeBlockedWithFewerThanThreeDefendingArtifacts() {
        Permanent ayesha = addReadyAyesha();
        harness.addToBattlefield(player2, new MindStone());
        harness.addToBattlefield(player2, new MindStone());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(ayesha))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyAyesha() {
        return addCreatureReady(player1, new AyeshaTanakaArmorer());
    }
}
