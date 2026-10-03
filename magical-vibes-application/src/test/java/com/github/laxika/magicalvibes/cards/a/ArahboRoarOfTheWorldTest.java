package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProwlingCaracal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ArahboRoarOfTheWorld.class, ProwlingCaracal.class, GrizzlyBears.class})
class ArahboRoarOfTheWorldTest extends BaseCardTest {

    @Test
    @DisplayName("Eminence gives another Cat +3/+3 at the beginning of combat")
    void eminenceBoostsAnotherCat() {
        addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, cat.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Eminence works while Arahbo is in the command zone")
    void eminenceWorksFromCommandZone() {
        addToCommandZone(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, cat.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(4);
    }

    @Test
    @DisplayName("Paying for another attacking Cat gives it trample and +X/+X")
    void payingForAttackingCatBoostsIt() {
        addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, cat.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Eminence does nothing if Arahbo moves to the command zone before resolution")
    void eminenceDoesNotFollowArahboToCommandZone() {
        Permanent arahbo = addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, cat.getId());
        harness.getPermanentRemovalService().removePermanentToCommandZone(gd, arahbo);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the attack payment leaves the Cat unchanged")
    void decliningAttackPaymentDoesNotBoostCat() {
        addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack bonus uses the Cat's power at resolution")
    void attackBonusUsesCurrentPower() {
        addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(1));
        cat.setPowerModifier(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Negative power gives a zero bonus but still grants trample")
    void negativePowerGivesZeroBonus() {
        addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());
        cat.setPowerModifier(-4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Arahbo and non-Cat attackers do not trigger the attack ability")
    void selfAndNonCatAttacksDoNotTrigger() {
        Permanent arahbo = addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, arahbo)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, arahbo, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Arahbo in the command zone does not trigger the attack ability")
    void commandZoneDoesNotGrantAttackBonus() {
        addToCommandZone(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Eminence does not trigger during an opponent's combat")
    void eminenceDoesNotTriggerOnOpponentTurn() {
        addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent cat = addCreatureReady(player1, new ProwlingCaracal());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each attacking Cat gets a separate optional payment")
    void multipleCatsGetSeparatePayments() {
        addCreatureReady(player1, new ArahboRoarOfTheWorld());
        Permanent first = addCreatureReady(player1, new ProwlingCaracal());
        Permanent second = addCreatureReady(player1, new ProwlingCaracal());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
