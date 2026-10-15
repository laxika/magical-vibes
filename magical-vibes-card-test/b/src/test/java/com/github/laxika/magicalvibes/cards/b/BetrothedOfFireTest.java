package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BetrothedOfFire.class, BenalishInfantry.class, BenalishKnight.class})
class BetrothedOfFireTest extends BaseCardTest {

    private Permanent attach(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BetrothedOfFire());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    void auraCanBeCastOnOpponentsCreature() {
        Permanent host = addCreatureReady(player2, new BenalishKnight());
        harness.setHand(player1, List.of(new BetrothedOfFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Betrothed of Fire").getAttachedTo())
                .isEqualTo(host.getId());
    }

    @Test
    @DisplayName("Sacrificing an untapped creature gives the enchanted creature +2/+0")
    void sacrificeUntappedCreatureBoostsEnchanted() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        bears.tap();
        attach(bears);
        addCreatureReady(player1, new BenalishInfantry());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Benalish Infantry");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        bears.tap();
        attach(bears);
        addCreatureReady(player1, new BenalishInfantry());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the first ability with no untapped creature to sacrifice")
    void cannotSacrificeWhenAllCreaturesTapped() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        bears.tap();
        attach(bears);
        Permanent elves = addCreatureReady(player1, new BenalishInfantry());
        elves.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("Sacrificing the enchanted creature gives creatures you control +2/+0")
    void sacrificeEnchantedBoostsTeam() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        attach(bears);
        Permanent elves = addCreatureReady(player1, new BenalishInfantry());
        Permanent opponentBears = addCreatureReady(player2, new BenalishKnight());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Benalish Knight");
        harness.assertNotOnBattlefield(player1, "Betrothed of Fire");
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice enchanted creature is paid when the second ability is activated")
    void sacrificeEnchantedCreatureIsActivationCost() {
        Permanent creature = addCreatureReady(player1, new BenalishKnight());
        attach(creature);

        harness.activateAbility(player1, 1, 1, null, null);

        harness.assertInGraveyard(player1, "Benalish Knight");
    }

    @Test
    void firstAbilityCanBoostOpponentsEnchantedCreature() {
        Permanent host = addCreatureReady(player2, new BenalishKnight());
        attach(host);
        harness.addToBattlefield(player1, new BenalishInfantry());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Benalish Infantry");
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    void cannotSacrificeOpponentsEnchantedCreature() {
        Permanent host = addCreatureReady(player2, new BenalishKnight());
        attach(host);
        harness.addToBattlefield(player1, new BenalishInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Benalish Knight");
        harness.assertOnBattlefield(player1, "Benalish Infantry");
        harness.assertOnBattlefield(player1, "Betrothed of Fire");
    }

    @Test
    void firstAbilityCanSacrificeItsOwnUntappedHost() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        attach(host);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Benalish Knight");
        harness.assertInGraveyard(player1, "Betrothed of Fire");
    }

    @Test
    void teamBoostAllowsTappedHostAndDoesNotAffectLaterCreatures() {
        Permanent host = addCreatureReady(player1, new BenalishKnight());
        host.tap();
        attach(host);
        Permanent recipient = addCreatureReady(player1, new BenalishInfantry());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Benalish Knight");
        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(3);
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(1);
    }
}
