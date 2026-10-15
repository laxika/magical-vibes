package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WestfoldRider.class, GrizzlyBears.class, LeoninScimitar.class, GloriousAnthem.class})
class WestfoldRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Westfold Rider destroys a target artifact")
    void sacrificesAndDestroysArtifact() {
        addCreatureReady(player1, new WestfoldRider());
        Permanent artifact = addReadyArtifact(player2);
        setSorcerySpeed();

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Westfold Rider");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Sacrificing Westfold Rider destroys a target enchantment")
    void sacrificesAndDestroysEnchantment() {
        addCreatureReady(player1, new WestfoldRider());
        Permanent enchantment = addReadyEnchantment(player2);
        setSorcerySpeed();

        harness.activateAbility(player1, 0, null, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Westfold Rider");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The activated ability cannot target a creature")
    void rejectsCreatureTarget() {
        addCreatureReady(player1, new WestfoldRider());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        setSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("The activated ability can only be used at sorcery speed")
    void rejectsActivationOutsideMainPhase() {
        addCreatureReady(player1, new WestfoldRider());
        Permanent artifact = addReadyArtifact(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void setSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = new Permanent(new LeoninScimitar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyEnchantment(Player player) {
        Permanent permanent = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
