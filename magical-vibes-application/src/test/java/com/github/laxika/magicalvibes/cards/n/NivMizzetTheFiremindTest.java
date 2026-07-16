package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NivMizzetTheFiremindTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Draw a card draws and triggers 1 damage to a chosen player")
    void tapDrawsAndDealsDamageToPlayer() {
        addReadyNiv(player1);
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Card was drawn
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Fugitive Wizard"));

        // Draw trigger awaits a target choice
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Draw trigger can deal its 1 damage to a creature, destroying a 1-toughness creature")
    void drawTriggerDamageDestroysCreature() {
        addReadyNiv(player1);
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FugitiveWizard());

        UUID wizardId = harness.getPermanentId(player2, "Fugitive Wizard");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, wizardId);
        harness.passBothPriorities();

        // The 1/1 target dies; the 2/2 is unharmed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    private Permanent addReadyNiv(Player player) {
        Permanent perm = new Permanent(new NivMizzetTheFiremind());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
