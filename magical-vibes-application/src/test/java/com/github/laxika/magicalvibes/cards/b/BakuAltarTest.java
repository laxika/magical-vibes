package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.cards.v.VitalSurge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BakuAltar.class, VitalSurge.class, TeardropKami.class, GoblinCohort.class})
class BakuAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger adds a ki counter for an Arcane spell")
    void arcaneSpellAddsKiCounterWhenAccepted() {
        Permanent altar = addAltar(player1);
        harness.castFromHand(player1, new VitalSurge(), "{1}{G}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger does not add a ki counter")
    void decliningTriggerDoesNotAddKiCounter() {
        Permanent altar = addAltar(player1);
        harness.castFromHand(player1, new VitalSurge(), "{1}{G}");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Accepting the trigger adds a ki counter for a Spirit spell")
    void spiritSpellAddsKiCounterWhenAccepted() {
        Permanent altar = addAltar(player1);
        harness.castFromHand(player1, new TeardropKami(), "{U}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger the altar")
    void nonSpiritNonArcaneSpellDoesNotTrigger() {
        Permanent altar = addAltar(player1);
        harness.castFromHand(player1, new GoblinCohort(), "{R}");

        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger the altar")
    void opponentSpiritSpellDoesNotTrigger() {
        Permanent altar = addAltar(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new TeardropKami(), "{U}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Paying two mana and removing a ki counter creates a Spirit token")
    void activationCreatesSpiritToken() {
        Permanent altar = addAltar(player1);
        altar.setCounterCount(CounterType.KI, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(altar.isTapped()).isTrue();
        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Spirit")
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && permanent.getCard().getColors().isEmpty());
    }

    @Test
    @DisplayName("The activation cannot be paid without a ki counter")
    void cannotActivateWithoutKiCounter() {
        addAltar(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAltar(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BakuAltar());
    }
}
