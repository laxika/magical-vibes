package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.s.ScrapskinDrake;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalvanicAlchemist.class, ScrapskinDrake.class, Cloudshift.class, SongOfTheDryads.class})
class GalvanicAlchemistTest extends BaseCardTest {

    private Permanent castAndPairWithDrake() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new ScrapskinDrake());
        harness.setHand(player1, List.of(new GalvanicAlchemist()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, drake.getId());
        return drake;
    }

    private void tap(Permanent permanent) {
        permanent.tap();
    }

    private void activateUntap(Permanent permanent) {
        harness.addMana(player1, ManaColor.BLUE, 3);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
        harness.activateAbility(player1, index, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulbond ETB pairs Galvanic Alchemist with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent drake = castAndPairWithDrake();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");

        assertThat(alchemist.getPairedWithId()).isEqualTo(drake.getId());
        assertThat(drake.getPairedWithId()).isEqualTo(alchemist.getId());
    }

    @Test
    @DisplayName("While paired, Galvanic Alchemist can untap itself")
    void pairedAlchemistCanUntapSelf() {
        castAndPairWithDrake();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        tap(alchemist);
        assertThat(alchemist.isTapped()).isTrue();

        activateUntap(alchemist);

        assertThat(findPermanent(player1, "Galvanic Alchemist").isTapped()).isFalse();
    }

    @Test
    @DisplayName("While paired, the partner can untap itself")
    void pairedPartnerCanUntapSelf() {
        Permanent drake = castAndPairWithDrake();
        tap(drake);
        assertThat(drake.isTapped()).isTrue();

        activateUntap(drake);

        assertThat(findPermanent(player1, "Scrapskin Drake").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unpaired Galvanic Alchemist does not have the untap ability")
    void unpairedCannotUntap() {
        harness.addToBattlefield(player1, new GalvanicAlchemist());
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        tap(alchemist);
        harness.addMana(player1, ManaColor.BLUE, 3);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(alchemist);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringWithoutAnotherUnpairedCreatureDoesNotTriggerSoulbond() {
        harness.setHand(player1, List.of(new GalvanicAlchemist()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Galvanic Alchemist");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void selfEnterPairingCanBeDeclined() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new ScrapskinDrake());
        harness.setHand(player1, List.of(new GalvanicAlchemist()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Galvanic Alchemist").getPairedWithId()).isNull();
        assertThat(partner.getPairedWithId()).isNull();
    }

    @Test
    void anotherCreatureEnteringCanBecomeThePartner() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new GalvanicAlchemist());
        harness.setHand(player1, List.of(new ScrapskinDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent partner = findPermanent(player1, "Scrapskin Drake");
        assertThat(alchemist.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(alchemist.getId());
        partner.tap();
        activateUntap(partner);
        assertThat(partner.isTapped()).isFalse();
    }

    @Test
    void anotherCreatureEnteringPairingCanBeDeclined() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new GalvanicAlchemist());
        harness.setHand(player1, List.of(new ScrapskinDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(alchemist.getPairedWithId()).isNull();
        assertThat(findPermanent(player1, "Scrapskin Drake").getPairedWithId()).isNull();
    }

    @Test
    void pairedCreatureDoesNotPairWithAThirdEnteringCreature() {
        Permanent partner = castAndPairWithDrake();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new ScrapskinDrake());

        assertThat(alchemist.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(alchemist.getId());
        assertThat(newcomer.getPairedWithId()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void opponentsEnteringCreatureDoesNotTriggerPairing() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new GalvanicAlchemist());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new ScrapskinDrake());

        assertThat(alchemist.getPairedWithId()).isNull();
        assertThat(opponentCreature.getPairedWithId()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void untappingPartnerDoesNotUntapAlchemist() {
        Permanent partner = castAndPairWithDrake();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        partner.tap();
        alchemist.tap();

        activateUntap(partner);

        assertThat(partner.isTapped()).isFalse();
        assertThat(alchemist.isTapped()).isTrue();
    }

    @Test
    void untapCostsTwoGenericAndOneBlueMana() {
        Permanent partner = castAndPairWithDrake();
        partner.tap();
        int partnerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(partner);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, partnerIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(partner.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, partnerIndex, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(partner.isTapped()).isFalse();
    }

    @Test
    void activatedUntapStillResolvesAfterAlchemistLeaves() {
        Permanent partner = castAndPairWithDrake();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        partner.tap();
        int partnerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(partner);
        harness.activateAbility(player1, partnerIndex, 0, null, null);
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, alchemist.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(partner.getPairedWithId()).isNull();
        harness.passBothPriorities();
        assertThat(partner.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, partnerIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pairingEndsWhenPartnerStopsBeingACreature() {
        Permanent partner = castAndPairWithDrake();
        Permanent alchemist = findPermanent(player1, "Galvanic Alchemist");
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, partner.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, partner)).isFalse();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(alchemist.getPairedWithId()).isNull();
        int alchemistIndex = gd.playerBattlefields.get(player1.getId()).indexOf(alchemist);
        assertThatThrownBy(() -> harness.activateAbility(player1, alchemistIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
