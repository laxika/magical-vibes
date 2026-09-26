package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrakeSkullCameo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuLair;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuraShards.class, DrakeSkullCameo.class, Forest.class, KavuLair.class,
        RagingKavu.class, YavimayaBarbarian.class})
class AuraShardsTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under your control may destroy a target artifact")
    void creatureEnteringMayDestroyArtifact() {
        harness.addToBattlefield(player1, new AuraShards());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DrakeSkullCameo());

        castYavimayaBarbarian(player1);
        resolveCreatureAndTrigger();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Drake-Skull Cameo");
    }

    @Test
    @DisplayName("A creature entering under your control may destroy a target enchantment")
    void creatureEnteringMayDestroyEnchantment() {
        harness.addToBattlefield(player1, new AuraShards());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new KavuLair());

        castYavimayaBarbarian(player1);
        resolveCreatureAndTrigger();

        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Kavu Lair");
    }

    @Test
    @DisplayName("Declining the trigger does not destroy the target")
    void declineDoesNotDestroyTarget() {
        harness.addToBattlefield(player1, new AuraShards());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DrakeSkullCameo());

        castYavimayaBarbarian(player1);
        resolveCreatureAndTrigger();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Drake-Skull Cameo");
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger Aura Shards")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new AuraShards());

        castRagingKavu(player2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Aura Shards itself is a legal target when no other artifact or enchantment is present")
    void auraShardsCanTargetItself() {
        Permanent shards = harness.addToBattlefieldAndReturn(player1, new AuraShards());
        harness.addToBattlefield(player2, new Forest());

        castYavimayaBarbarian(player1);
        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction(com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(shards.getId());
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A creature entering may destroy an artifact its controller controls")
    void creatureEnteringMayDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new AuraShards());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DrakeSkullCameo());

        castYavimayaBarbarian(player1);
        resolveCreatureAndTrigger();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Drake-Skull Cameo");
    }

    private void castYavimayaBarbarian(Player player) {
        harness.setHand(player, List.of(new YavimayaBarbarian()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castCreature(player, 0);
    }

    private void castRagingKavu(Player player) {
        harness.setHand(player, List.of(new RagingKavu()));
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castCreature(player, 0);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
    }
}
