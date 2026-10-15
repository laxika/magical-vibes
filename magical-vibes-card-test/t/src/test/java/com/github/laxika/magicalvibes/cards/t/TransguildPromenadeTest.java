package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TransguildPromenade.class})
class TransguildPromenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        playTransguildPromenade();

        assertThat(findPromenade(player1)).isNotNull();
        assertThat(findPromenade(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} keeps Transguild Promenade on the battlefield")
    void payingKeepsIt() {
        playTransguildPromenade();
        resolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPromenade(player1)).isNotNull();
        harness.assertNotInGraveyard(player1, "Transguild Promenade");
    }

    @Test
    @DisplayName("Declining to pay {1} sacrifices Transguild Promenade")
    void decliningSacrificesIt() {
        playTransguildPromenade();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPromenade(player1)).isNull();
        harness.assertInGraveyard(player1, "Transguild Promenade");
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAddsChosenColorMana() {
        Permanent promenade = addPromenadeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(promenade.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void playTransguildPromenade() {
        harness.setHand(player1, List.of(new TransguildPromenade()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPromenadeReady(Player player) {
        Permanent promenade = harness.addToBattlefieldAndReturn(player, new TransguildPromenade());
        promenade.setSummoningSick(false);
        return promenade;
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN", "COLORLESS"})
    void genericEntryPaymentConsumesExactlyOneMana(ManaColor color) {
        playTransguildPromenade();
        resolveEnterTrigger();
        harness.addMana(player1, color, 2);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Transguild Promenade");
        harness.assertNotInGraveyard(player1, "Transguild Promenade");
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(findPromenade(player1).isTapped()).isTrue();
    }

    @Test
    void acceptingWithoutManaStillSacrificesIt() {
        playTransguildPromenade();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Transguild Promenade");
        harness.assertInGraveyard(player1, "Transguild Promenade");
    }

    @Test
    void decliningSacrificesOnlyTheEnteringPromenade() {
        Permanent existing = addPromenadeReady(player1);
        playTransguildPromenade();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Transguild Promenade")).containsExactly(existing);
        harness.assertInGraveyard(player1, "Transguild Promenade");
    }

    @Test
    void decliningWithManaAvailableDoesNotSpendIt() {
        playTransguildPromenade();
        resolveEnterTrigger();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Transguild Promenade");
        harness.assertInGraveyard(player1, "Transguild Promenade");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "GREEN"})
    void tapAddsEachOtherColorWithoutUsingTheStack(ManaColor color) {
        Permanent promenade = addPromenadeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(promenade.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent findPromenade(Player player) {
        return findPermanents(player, "Transguild Promenade").stream().findFirst().orElse(null);
    }
}
