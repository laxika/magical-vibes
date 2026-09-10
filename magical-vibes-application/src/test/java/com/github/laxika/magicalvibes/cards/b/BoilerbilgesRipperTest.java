package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoilerbilgesRipper.class, AuraOfSilence.class, GrizzlyBears.class})
class BoilerbilgesRipperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to deal 2 damage to a player")
    void etbSacrificesCreatureAndDealsDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castRipperToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB may sacrifice another enchantment to deal 2 damage")
    void etbSacrificesEnchantmentAndDealsDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AuraOfSilence());
        int lifeBefore = gd.getLife(player2.getId());

        castRipperToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertNotOnBattlefield(player1, "Aura of Silence");
    }

    @Test
    @DisplayName("Declining the ETB sacrifice deals no damage")
    void decliningEtbSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castRipperToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
    }

    private void castRipperToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BoilerbilgesRipper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
