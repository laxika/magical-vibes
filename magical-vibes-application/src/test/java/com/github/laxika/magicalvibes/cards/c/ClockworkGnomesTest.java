package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.j.JovensTools;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.cards.r.RysorianBadger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClockworkGnomes.class, Roterothopter.class, RysorianBadger.class, JovensTools.class, AnabaShaman.class})
class ClockworkGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield to the target artifact creature")
    void resolvingGrantsShield() {
        setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());
        harness.passBothPriorities();

        assertThat(thopter.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's artifact creature")
    void canTargetOpponentArtifactCreature() {
        setupGnomes();
        Permanent thopter = addArtifactCreature(player2);

        harness.activateAbility(player1, 0, null, thopter.getId());
        harness.passBothPriorities();

        assertThat(thopter.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating taps Clockwork Gnomes")
    void tapsOnActivation() {
        Permanent gnomes = setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());

        assertThat(gnomes.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can regenerate itself")
    void canRegenerateSelf() {
        Permanent gnomes = setupGnomes();

        harness.activateAbility(player1, 0, null, gnomes.getId());
        harness.passBothPriorities();

        assertThat(gnomes.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires three generic mana to activate")
    void requiresThreeGenericMana() {
        Permanent gnomes = addCreatureReady(player1, new ClockworkGnomes());
        Permanent thopter = addArtifactCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thopter.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gnomes.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The regeneration shield saves the target from lethal damage")
    void shieldSavesTargetFromLethalDamage() {
        setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());
        harness.passBothPriorities();

        addCreatureReady(player1, new AnabaShaman());
        addCreatureReady(player1, new AnabaShaman());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 2, null, thopter.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 3, null, thopter.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roterothopter");
        assertThat(thopter.isTapped()).isTrue();
        assertThat(thopter.getRegenerationShield()).isZero();
        assertThat(thopter.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        setupGnomes();
        Permanent badger = addCreatureReady(player1, new RysorianBadger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, badger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        setupGnomes();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new JovensTools());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());
        gd.playerBattlefields.get(player1.getId()).remove(thopter);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(thopter.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Clockwork Gnomes cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent gnomes = setupGnomes();
        gnomes.setSummoningSick(true);
        Permanent thopter = addArtifactCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thopter.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gnomes.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Clockwork Gnomes cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent gnomes = setupGnomes();
        gnomes.tap();
        Permanent thopter = addArtifactCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thopter.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Colored mana pays the generic activation cost and the shield does not tap the target")
    void coloredManaPaysGenericCost() {
        addCreatureReady(player1, new ClockworkGnomes());
        Permanent thopter = addArtifactCreature(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(thopter.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(thopter.getRegenerationShield()).isEqualTo(1);
        assertThat(thopter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability still resolves after Clockwork Gnomes leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent gnomes = setupGnomes();
        Permanent thopter = addArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, thopter.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gnomes));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Clockwork Gnomes");
        assertThat(thopter.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent setupGnomes() {
        Permanent gnomes = addCreatureReady(player1, new ClockworkGnomes());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        return gnomes;
    }

    private Permanent addArtifactCreature(Player player) {
        return addCreatureReady(player, new Roterothopter());
    }
}
