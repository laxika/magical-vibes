package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuptureSpire.class})
class RuptureSpireTest extends BaseCardTest {

    private void playRuptureSpire() {
        harness.setHand(player1, List.of(new RuptureSpire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities(); // ETB "sacrifice unless pay {1}" trigger onto the stack
        harness.passBothPriorities(); // resolve it -> may-pay prompt
    }

    private Permanent ruptureSpire() {
        return findPermanents(player1, "Rupture Spire").stream().findFirst().orElse(null);
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
        harness.assertNotInGraveyard(player1, "Rupture Spire");
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices Rupture Spire")
    void decliningSacrificesIt() {
        playRuptureSpire();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(ruptureSpire()).isNull();
        harness.assertInGraveyard(player1, "Rupture Spire");
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

    @Test
    @DisplayName("Accepting payment without mana still sacrifices Rupture Spire")
    void cannotKeepSpireWithoutPaying() {
        playRuptureSpire();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Rupture Spire");
        harness.assertInGraveyard(player1, "Rupture Spire");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Any colored mana can pay the generic entry cost and exactly one mana is spent")
    void coloredManaPaysEntryCost(ManaColor color) {
        harness.addMana(player1, color, 2);
        playRuptureSpire();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Rupture Spire");
        assertThat(ruptureSpire().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Rupture Spire");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "GREEN"})
    @DisplayName("The mana ability produces each other color immediately")
    void tapAddsOtherColors(ManaColor color) {
        harness.addToBattlefield(player1, new RuptureSpire());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(ruptureSpire().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining payment with mana available leaves that mana unspent")
    void decliningDoesNotSpendMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        playRuptureSpire();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Rupture Spire");
        harness.assertInGraveyard(player1, "Rupture Spire");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
