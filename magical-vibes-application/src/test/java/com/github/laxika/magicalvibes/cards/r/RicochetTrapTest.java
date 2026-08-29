package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RicochetTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot use the alternate cost unless an opponent cast a blue spell this turn")
    void alternateCostRequiresOpponentBlueSpell() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player2, target);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang, new RicochetTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, boomerang.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the alternate cost after an opponent casts a blue spell and changes a spell's target")
    void usesAlternateCostAndChangesTarget() {
        GrizzlyBears player1Bears = new GrizzlyBears();
        GrizzlyBears player2Bears = new GrizzlyBears();
        harness.addToBattlefield(player1, player1Bears);
        harness.addToBattlefield(player2, player2Bears);
        UUID player1BearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID player2BearsId = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1BearsId);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RicochetTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstantWithAlternateCost(player1, 0, boomerang.getId(), List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2BearsId);

        harness.handlePermanentChosen(player1, player2BearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's non-blue spell does not enable the alternate cost")
    void alternateCostDoesNotUseNonBlueSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RicochetTrap()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, lavaAxe.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
