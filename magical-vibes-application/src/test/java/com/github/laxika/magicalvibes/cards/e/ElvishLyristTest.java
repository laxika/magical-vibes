package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.c.Caltrops;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TolarianAcademy;
import com.github.laxika.magicalvibes.cards.v.VoltaicKey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianSwine.class, Caltrops.class, ElvishLyrist.class, GloriousAnthem.class, GrizzlyBears.class, Island.class, TolarianAcademy.class, VoltaicKey.class})
class ElvishLyristTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Elvish Lyrist and destroys target enchantment")
    void destroysTargetEnchantment() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elvish Lyrist");
        harness.assertInGraveyard(player1, "Elvish Lyrist");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness (tap cost)")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ElvishLyrist());
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness (tap cost)")
    void cannotActivateWithSummoningSicknessUpstreamReview() {
        harness.addToBattlefield(player1, new ElvishLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent creature = addCreatureReady(player2, new ArgothianSwine());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        addReadyLyrist(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifactUpstreamReview() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new VoltaicKey());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TolarianAcademy());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target enchantment leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private Permanent addReadyLyrist(Player player) {
        return addCreatureReady(player, new ElvishLyrist());
    }


    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GloriousAnthem());
    }


    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Caltrops());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }


    @Test
    @DisplayName("Pays the sacrifice cost when the ability is activated")
    void paysSacrificeCostOnActivation() {
        addCreatureReady(player1, new ElvishLyrist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Elvish Lyrist");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.stack).hasSize(1);
    }
}
