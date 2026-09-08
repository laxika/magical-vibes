package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedServitor.class, Shock.class})
class BarbedServitorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield suspects Barbed Servitor")
    void enteringTheBattlefieldSuspectsIt() {
        Permanent servitor = castServitor(player1);

        assertThat(servitor.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, servitor, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, servitor)).isFalse();
    }

    @Test
    @DisplayName("Combat damage draws a card and makes its controller lose 1 life")
    void combatDamageDrawsAndLosesLife() {
        Permanent servitor = addReadyServitor(player1);
        servitor.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Being dealt damage makes a chosen opponent lose that much life")
    void dealtDamageMakesChosenOpponentLoseThatMuchLife() {
        Permanent servitor = addReadyServitor(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, servitor.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player2.getId()).doesNotContain(player1.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Barbed Servitor");
    }

    private Permanent castServitor(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new BarbedServitor()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        resolveAllTriggers();
        return findPermanent(player, "Barbed Servitor");
    }

    private Permanent addReadyServitor(com.github.laxika.magicalvibes.model.Player player) {
        Permanent servitor = harness.addToBattlefieldAndReturn(player, new BarbedServitor());
        servitor.setSummoningSick(false);
        return servitor;
    }
}
