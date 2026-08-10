package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FadeAwayTest extends BaseCardTest {

    @Test
    @DisplayName("A player can pay for some creatures and sacrifice other permanents for the rest")
    void paysForSomeCreaturesAndSacrificesOtherPermanent() {
        Permanent keptCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new FadeAway()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(keptCreature.getId()));

        harness.handleMultiplePermanentsChosen(player2, List.of(millstone.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Millstone");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sacrifices are deferred until every player has made their choice")
    void sacrificesAreDeferredUntilAllPlayersChoose() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new IronStar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player1, List.of(new FadeAway()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(ownArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, ownArtifact);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentArtifact.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Iron Star");
        harness.assertInGraveyard(player2, "Millstone");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
    }
}
