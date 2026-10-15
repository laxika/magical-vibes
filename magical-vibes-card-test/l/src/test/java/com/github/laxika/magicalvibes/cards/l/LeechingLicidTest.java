package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeechingLicid.class, TrainedArmodon.class, Forest.class, DarkBanishing.class})
class LeechingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature and stops it being a creature")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature's controller takes 1 damage at their upkeep")
    void upkeepDamagesEnchantedCreaturesController() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and stops the upkeep trigger")
    void endCostRevertsLicidAndStopsTrigger() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();

        harness.setLife(player2, 20);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Paying the end cost reverts the Aura immediately")
    void payingEndCostIsImmediate() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles and the Licid stays a creature if the target leaves")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        gd.playerBattlefields.get(player2.getId()).remove(host);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    @Test
    @DisplayName("An unattached Licid does not deal upkeep damage")
    void creatureFormDoesNotTrigger() {
        addReadyLicid(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Enchanting your own creature damages you at your upkeep")
    void ownCreatureControllerTakesDamage() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Enchanting an opponent's creature does not trigger on your upkeep")
    void wrongPlayersUpkeepDoesNotTrigger() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ending the Aura effect does not stop an upkeep trigger already on the stack")
    void pendingUpkeepTriggerSurvivesEndPayment() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A Licid targeting itself becomes an unattached Aura and goes to the graveyard")
    void targetingItselfCannotKeepAuraOnBattlefield() {
        Permanent licid = addReadyLicid(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, licid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leeching Licid");
        harness.assertInGraveyard(player1, "Leeching Licid");
    }

    @Test
    @DisplayName("An attached Licid goes to the graveyard when its host is destroyed")
    void destroyedHostLeavesLicidInGraveyard() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, host.getId());

        harness.assertInGraveyard(player2, "Trained Armodon");
        harness.assertNotOnBattlefield(player1, "Leeching Licid");
        harness.assertInGraveyard(player1, "Leeching Licid");
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new LeechingLicid());
    }
}
