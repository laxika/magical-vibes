package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuptureSpireTest extends BaseCardTest {

    private void playRuptureSpire() {
        harness.setHand(player1, List.of(new RuptureSpire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities(); // ETB "sacrifice unless pay {1}" trigger onto the stack
        harness.passBothPriorities(); // resolve it -> may-pay prompt
    }

    private Permanent ruptureSpire() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rupture Spire"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        playRuptureSpire();

        assertThat(ruptureSpire()).isNotNull();
        assertThat(ruptureSpire().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} keeps Rupture Spire on the battlefield")
    void payingKeepsIt() {
        playRuptureSpire();
        resolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ruptureSpire()).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Rupture Spire"));
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices Rupture Spire")
    void decliningSacrificesIt() {
        playRuptureSpire();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(ruptureSpire()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rupture Spire"));
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAddsChosenColorMana() {
        harness.addToBattlefield(player1, new RuptureSpire());
        Permanent spire = ruptureSpire();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(spire.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
