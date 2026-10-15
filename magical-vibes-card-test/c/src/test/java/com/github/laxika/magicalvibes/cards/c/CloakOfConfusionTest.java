package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloakOfConfusion.class, GrizzlyBears.class, Forest.class, Skullcrack.class})
class CloakOfConfusionTest extends BaseCardTest {

    private Permanent addEnchantedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent aura = new Permanent(new CloakOfConfusion());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return attacker;
    }

    private void advanceToUnblockedTrigger() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting: enchanted attacker deals no combat damage and defender discards at random")
    void acceptAssignsNoDamageAndForcesDiscard() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        addEnchantedAttacker();

        advanceToUnblockedTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(((PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Defender lost one card at random and the enchanted attacker dealt no combat damage.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining: enchanted attacker deals combat damage and defender keeps their cards")
    void declineDealsDamageAndNoDiscard() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        addEnchantedAttacker();

        advanceToUnblockedTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        // No discard, and the 2/2 attacker dealt its combat damage to the defending player.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Accepting with an empty hand still prevents combat damage")
    void acceptWithEmptyHandStillPreventsDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        addEnchantedAttacker();

        advanceToUnblockedTrigger();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked enchanted attacker does not trigger the discard")
    void blockedDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        Permanent attacker = addEnchantedAttacker();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        // The attacker was blocked, so no unblocked-attack trigger fired: no may prompt, no discard.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot enchant a creature you do not control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        // A creature you control makes the Aura playable, so casting reaches target validation.
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CloakOfConfusion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Accepting assigns no damage even when damage cannot be prevented")
    void acceptingStillAssignsNoDamageWhenDamageCannotBePrevented() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        addEnchantedAttacker();

        advanceToUnblockedTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Accepting only stops the enchanted creature's damage")
    void otherUnblockedAttackerStillDealsDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        addEnchantedAttacker();
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        otherAttacker.setAttacking(true);

        advanceToUnblockedTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can enchant a creature you control")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CloakOfConfusion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cloak of Confusion").getAttachedTo())
                .isEqualTo(creature.getId());
    }
}
