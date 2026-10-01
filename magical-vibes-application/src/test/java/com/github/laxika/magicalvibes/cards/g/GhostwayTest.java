package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SkarrgTheRagePits;
import com.github.laxika.magicalvibes.cards.s.SkySwallower;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ghostway.class, GhostWarden.class, SkarrgTheRagePits.class, SkySwallower.class})
class GhostwayTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each creature you control, but not lands or opposing creatures")
    void exilesOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new GhostWarden());
        harness.addToBattlefield(player1, new GhostWarden());
        harness.addToBattlefield(player1, new SkarrgTheRagePits());
        harness.addToBattlefield(player2, new GhostWarden());

        castGhostway(player1);

        harness.assertNotOnBattlefield(player1, "Ghost Warden");
        harness.assertOnBattlefield(player1, "Skarrg, the Rage Pits");
        harness.assertOnBattlefield(player2, "Ghost Warden");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost Warden", "Ghost Warden");
    }

    @Test
    @DisplayName("Exiled creatures return under their owners' control at the next end step")
    void creaturesReturnAtNextEndStep() {
        harness.addToBattlefield(player1, new GhostWarden());

        castGhostway(player1);
        advanceToEndStep(player1);

        harness.assertOnBattlefield(player1, "Ghost Warden");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns a creature controlled by another player under its owner's control")
    void creatureReturnsUnderItsOwnersControl() {
        Permanent ownedCreature = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        harness.addToBattlefield(player1, new SkarrgTheRagePits());

        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownedCreature);

        castGhostway(player2);

        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertOnBattlefield(player2, "Skarrg, the Rage Pits");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Ghost Warden");

        advanceToEndStep(player2);

        harness.assertOnBattlefield(player1, "Ghost Warden");
        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castGhostway(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(caster, new Ghostway(), "{2}{W}");
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
