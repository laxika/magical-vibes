package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagesAttendant.class, Shock.class, CruelEdict.class, GrizzlyBears.class})
class MagesAttendantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and creates a Wizard token")
    void createsWizardToken() {
        castMagesAttendant();

        Permanent wizard = findPermanents(player1, "Wizard").getFirst();
        assertThat(wizard.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Wizard token counters a noncreature spell when its controller cannot pay")
    void countersNoncreatureSpellWhenControllerCannotPay() {
        castMagesAttendant();

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        Permanent wizard = findPermanents(player1, "Wizard").getFirst();
        int wizardIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wizard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, wizardIndex, null, shock.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gameData.stack).isEmpty();
        assertThat(findPermanents(player1, "Wizard")).isEmpty();
    }

    @Test
    @DisplayName("Wizard token does not counter a noncreature spell when its controller pays")
    void doesNotCounterWhenControllerPays() {
        castMagesAttendant();

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        Permanent wizard = findPermanents(player1, "Wizard").getFirst();
        int wizardIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wizard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, wizardIndex, null, shock.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Wizard")).isEmpty();
    }

    @Test
    @DisplayName("Wizard token cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        castMagesAttendant();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        Permanent wizard = findPermanents(player1, "Wizard").getFirst();
        int wizardIndex = gd.playerBattlefields.get(player1.getId()).indexOf(wizard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, wizardIndex, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMagesAttendant() {
        harness.setHand(player1, List.of(new MagesAttendant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
