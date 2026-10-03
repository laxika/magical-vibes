package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindingSouleater.class, Forest.class, PristineTalisman.class})
class BlindingSouleaterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature when paying with white mana")
    void tapsTargetCreatureWithWhiteMana() {
        addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("White mana is consumed when activating ability")
    void whiteManaConsumed() {
        addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can pay Phyrexian mana with 2 life when no white mana available")
    void paysLifeWhenNoWhiteMana() {
        addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.setLife(player1, 20);
        // No mana added — Phyrexian mana auto-pays with 2 life

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prefers white mana over life payment when available")
    void prefersWhiteManaOverLife() {
        addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadySouleater(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target a non-creature artifact")
    void cannotTargetArtifact() {
        addReadySouleater(player1);
        Permanent artifact = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        addReadySouleater(player1);
        Permanent ownCreature = addCreatureReady(player1, new BlindingSouleater());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Blinding Souleater")
    void activatingTapsSouleater() {
        Permanent souleater = addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(souleater.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent souleater = addReadySouleater(player1);
        souleater.tap();
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate when summoning sick (it is a creature)")
    void cannotActivateWhenSummoningSick() {
        Permanent souleater = harness.addToBattlefieldAndReturn(player1, new BlindingSouleater());
        souleater.setSummoningSick(true);

        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can target itself even though paying the tap cost taps it")
    void canTargetItself() {
        Permanent souleater = addReadySouleater(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, souleater.getId());
        harness.passBothPriorities();

        assertThat(souleater.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    @DisplayName("A tapped creature remains a legal target")
    void canTargetTappedCreature() {
        addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent souleater = addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(souleater);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot pay two life with only one life and no white mana")
    void cannotPayMoreLifeThanAvailable() {
        Permanent souleater = addReadySouleater(player1);
        Permanent target = addCreatureReady(player2, new BlindingSouleater());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(souleater.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySouleater(Player player) {
        return addCreatureReady(player, new BlindingSouleater());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PristineTalisman());
    }
}
