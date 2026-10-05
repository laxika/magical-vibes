package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({QuickeningLicid.class, TrainedArmodon.class, Forest.class, DarkBanishing.class})
class QuickeningLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature and stops it being a creature")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("Attached Licid gives the enchanted creature first strike")
    void attachedLicidGrantsFirstStrike() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("It can attach to an opponent's creature")
    void canAttachToOpponentCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and turns it back into a creature")
    void endCostRevertsLicidToCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("While an Aura the original Licid ability cannot be activated")
    void originalLicidAbilityCannotBeActivatedWhileAttached() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @Test
    @DisplayName("Paying the end cost reverts the Aura immediately")
    void payingEndCostIsImmediate() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles and the Licid stays a creature if the target leaves")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Targeting itself puts the transformed Licid into its owner's graveyard")
    void selfTargetingPutsLicidIntoGraveyard() {
        Permanent licid = addReadyLicid(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, licid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Quickening Licid");
        harness.assertInGraveyard(player1, "Quickening Licid");
    }

    @Test
    @DisplayName("The Licid goes to the graveyard when its enchanted creature is destroyed")
    void destroyedHostPutsLicidIntoGraveyard() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, host.getId());

        harness.assertInGraveyard(player2, "Trained Armodon");
        harness.assertNotOnBattlefield(player1, "Quickening Licid");
        harness.assertInGraveyard(player1, "Quickening Licid");
    }

    @Test
    @DisplayName("A Licid destroyed in response does not grant first strike to its target")
    void destroyedSourceDoesNotEnchantTarget() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, host.getId());

        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, licid.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quickening Licid");
        harness.assertOnBattlefield(player1, "Trained Armodon");
        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Licid cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent licid = harness.addToBattlefieldAndReturn(player1, new QuickeningLicid());
        licid.setSummoningSick(true);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Ending the effect requires white mana even though it does not use the stack")
    void cannotEndEffectWithOnlyColorlessMana() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new QuickeningLicid());
    }
}
