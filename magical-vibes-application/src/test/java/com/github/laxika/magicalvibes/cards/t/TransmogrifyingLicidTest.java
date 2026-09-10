package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TransmogrifyingLicid.class, ElvishBerserker.class, Spellbook.class})
class TransmogrifyingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature and makes it an Aura")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
        assertThat(gqs.isArtifact(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("Ability can attach the Licid to an opponent's creature")
    void abilityCanTargetOpponentCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new ElvishBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, host)).isTrue();
    }

    @Test
    @DisplayName("Attached Licid gives the enchanted creature +1/+1 and makes it an artifact")
    void attachedLicidBoostsAndMakesArtifact() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, host)).isTrue();
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and removes its granted effects")
    void endCostRevertsLicidToCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, host)).isFalse();
    }

    @Test
    @DisplayName("An illegal target on resolution leaves the Licid as a creature")
    void illegalTargetOnResolutionLeavesLicidUnchanged() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        gd.playerBattlefields.get(player1.getId()).remove(host);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(licid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ending the effect is immediate and does not use the stack")
    void endingEffectIsSpecialAction() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        addReadyLicid(player1);
        Permanent artifact = addNonCreaturePermanent(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addNonCreaturePermanent(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Spellbook());
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new TransmogrifyingLicid());
    }
}
