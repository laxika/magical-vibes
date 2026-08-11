package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuraShardsTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under your control may destroy a target artifact")
    void creatureEnteringMayDestroyArtifact() {
        harness.addToBattlefield(player1, new AuraShards());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        castGrizzlyBears();
        resolveCreatureAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("A creature entering under your control may destroy a target enchantment")
    void creatureEnteringMayDestroyEnchantment() {
        harness.addToBattlefield(player1, new AuraShards());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castGrizzlyBears();
        resolveCreatureAndTrigger();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, enchantment.getId());

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Declining the trigger does not destroy the target")
    void declineDoesNotDestroyTarget() {
        harness.addToBattlefield(player1, new AuraShards());
        harness.addToBattlefield(player2, new LeoninScimitar());

        castGrizzlyBears();
        resolveCreatureAndTrigger();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger Aura Shards")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new AuraShards());

        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new AuraShards());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castGrizzlyBears();
        resolveCreatureAndTrigger();

        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
