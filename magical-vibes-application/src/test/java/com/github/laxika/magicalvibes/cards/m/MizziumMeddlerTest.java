package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MizziumMeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Flashed in, it redirects a targeted spell to itself")
    void redirectsTargetedSpellToItself() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        // Resolve the Meddler → it enters and its ETB trigger asks for a stack target.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(boomerang.getId());
        harness.handlePermanentChosen(player1, boomerang.getId());

        // Resolve the ETB trigger (redirect), then Boomerang itself.
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mizzium Meddler");
    }

    @Test
    @DisplayName("An activated ability on the stack is a legal target too")
    void redirectsActivatedAbility() {
        ProdigalSorcerer sorcerer = new ProdigalSorcerer();
        Permanent sorcererPerm = new Permanent(sorcerer);
        sorcererPerm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(sorcererPerm);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesPermId = harness.getPermanentId(player1, "Llanowar Elves");

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, elvesPermId);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(sorcerer.getId());
        harness.handlePermanentChosen(player1, sorcerer.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        // The 1 damage hit the 1/4 Meddler instead of the 1/1 Elves.
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Mizzium Meddler");
    }

    @Test
    @DisplayName("Nothing changes when the Meddler is not a legal target for the spell")
    void doesNothingWhenNotALegalTarget() {
        harness.forceActivePlayer(player2);
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new MizziumMeddler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handlePermanentChosen(player1, lavaAxe.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        // Lava Axe targets a player; the Meddler is not a legal new target.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }
}
