package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.SearingBlaze;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RicochetTrap.class, Boomerang.class, GrizzlyBears.class, LavaAxe.class, Cancel.class, SearingBlaze.class})
class RicochetTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot use the alternate cost unless an opponent cast a blue spell this turn")
    void alternateCostRequiresOpponentBlueSpell() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
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
        UUID player1BearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID player2BearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

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

    @Test
    @DisplayName("Normal cost works without a blue spell and redirects a player target")
    void normalCostRedirectsPlayerTarget() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RicochetTrap()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, lavaAxe.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a spell with no targets")
    void rejectsUntargetedSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new RicochetTrap()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Cannot target a spell with two targets")
    void rejectsMultipleTargets() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        SearingBlaze blaze = new SearingBlaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, List.of(player1.getId(), bearsId));
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RicochetTrap()));
        harness.addMana(player1, ManaColor.RED, 4);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, blaze.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("A counterspell can be redirected to the resolving Ricochet Trap")
    void redirectsCounterspellToResolvingTrap() {
        GrizzlyBears bears = new GrizzlyBears();
        Cancel cancel = new Cancel();
        harness.setHand(player1, List.of(bears, new RicochetTrap()));
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);

        UUID trapId = gd.playerHands.get(player1.getId()).getFirst().getId();
        harness.castInstantWithAlternateCost(player1, 0, cancel.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(trapId)
                .doesNotContain(cancel.getId(), bears.getId());
        harness.handlePermanentChosen(player1, trapId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ricochet Trap");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("A resolved opponent blue spell enables the alternate cost for a different nonblue spell")
    void resolvedBlueSpellEnablesUnrelatedTarget() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, bearsId);

        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RicochetTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstantWithAlternateCost(player1, 0, lavaAxe.getId(), List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
