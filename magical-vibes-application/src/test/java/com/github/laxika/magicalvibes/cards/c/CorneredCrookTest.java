package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorneredCrook.class, Ornithopter.class, GrizzlyBears.class})
class CorneredCrookTest extends BaseCardTest {

    private void castCrookToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CorneredCrook()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrificing an artifact deals 3 damage to a target player")
    void sacrificeArtifactDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.getLife(player2.getId());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Sacrificing an artifact deals 3 damage to a target creature")
    void sacrificeArtifactDealsDamageToCreature() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the sacrifice deals no damage and keeps the artifact")
    void decliningSacrificeDealsNoDamage() {
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.getLife(player2.getId());

        castCrookToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Ornithopter");
    }
}
