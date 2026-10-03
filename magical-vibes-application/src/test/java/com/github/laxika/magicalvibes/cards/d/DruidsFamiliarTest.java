package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidsFamiliar.class, GrizzlyBears.class})
class DruidsFamiliarTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DruidsFamiliar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent familiar() {
        return findPermanent(player1, "Druid's Familiar");
    }

    @Test
    @DisplayName("Soulbond ETB pairs Druid's Familiar with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent familiar = familiar();

        assertThat(familiar.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(familiar.getId());
    }

    @Test
    @DisplayName("While paired, both creatures get +2/+2")
    void pairedBothGetBoost() {
        Permanent bears = castAndPairWithBears();
        Permanent familiar = familiar();

        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Unpaired Druid's Familiar gets no boost")
    void unpairedHasNoBoost() {
        harness.addToBattlefield(player1, new DruidsFamiliar());
        Permanent familiar = familiar();

        assertThat(familiar.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired and unboosted")
    void decliningLeavesUnpairedWithoutBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DruidsFamiliar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent familiar = familiar();
        assertThat(familiar.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unpaired third creature is not boosted")
    void unpairedBystanderIsNotBoosted() {
        castAndPairWithBears();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Soulbond does not trigger when Familiar enters alone")
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.setHand(player1, List.of(new DruidsFamiliar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(familiar().getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("An opponent's creature cannot enable the entry soulbond trigger")
    void opponentsCreatureDoesNotEnableEntryTrigger() {
        harness.addToBattlefield(player2, new DruidsFamiliar());
        harness.setHand(player1, List.of(new DruidsFamiliar()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An unpaired Familiar can pair with a creature entering later")
    void pairsWithLaterEnteringCreature() {
        harness.addToBattlefield(player1, new DruidsFamiliar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent familiar = familiar();
        assertThat(familiar.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(familiar.getId());
        assertThat(gqs.getEffectivePower(gd, familiar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, familiar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Pairing with a creature entering later is optional")
    void canDeclineLaterEnteringCreature() {
        harness.addToBattlefield(player1, new DruidsFamiliar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(familiar().getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, familiar())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A paired Familiar cannot switch partners when another creature enters")
    void pairedFamiliarCannotSwitchPartners() {
        Permanent partner = castAndPairWithBears();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(familiar().getPairedWithId()).isEqualTo(partner.getId());
    }

    @Test
    @CardUsed(PeelFromReality.class)
    @DisplayName("Familiar loses its bonus immediately when its partner leaves")
    void returningPartnerBreaksPairAndRemovesBoost() {
        Permanent partner = castAndPairWithBears();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DruidsFamiliar());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(partner.getId(), opponent.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(familiar().getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, familiar())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, familiar())).isEqualTo(2);
    }

    @Test
    @CardUsed(PeelFromReality.class)
    @DisplayName("The partner loses its bonus immediately when Familiar leaves")
    void returningFamiliarRemovesPartnersBoost() {
        Permanent partner = castAndPairWithBears();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DruidsFamiliar());
        harness.setHand(player1, List.of(new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, List.of(familiar().getId(), opponent.getId()));

        harness.assertNotOnBattlefield(player1, "Druid's Familiar");
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, partner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, partner)).isEqualTo(2);
    }
}
