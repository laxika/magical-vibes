package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SproutingPhytohydra.class, SealOfFire.class, SimicRagworm.class})
class SproutingPhytohydraTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the damage trigger creates a token copy")
    void acceptingDamageTriggerCreatesTokenCopy() {
        UUID phytohydraId = addPhytohydraAndSealOfFire();

        harness.activateAbility(player1, 0, null, phytohydraId);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the damage trigger creates no token")
    void decliningDamageTriggerCreatesNoToken() {
        UUID phytohydraId = addPhytohydraAndSealOfFire();

        harness.activateAbility(player1, 0, null, phytohydraId);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(tokenCount()).isZero();
    }

    @Test
    @DisplayName("Token copies retain the damage trigger")
    void tokenCopiesRetainDamageTrigger() {
        UUID phytohydraId = addPhytohydraAndSealOfFire();

        harness.activateAbility(player1, 0, null, phytohydraId);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        UUID tokenId = findPermanents(player2, "Sprouting Phytohydra").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(permanent -> permanent.getId())
                .findFirst()
                .orElseThrow();
        harness.activateAbility(player1, 0, null, tokenId);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage also creates a token copy")
    void combatDamageCreatesTokenCopy() {
        addCreatureReady(player1, new SimicRagworm());
        addCreatureReady(player2, new SproutingPhytohydra());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Separate damage events each create one copy, even when the second is lethal")
    void separateDamageEventsEachCreateOneCopy() {
        UUID phytohydraId = addPhytohydraAndSealOfFire();
        findPermanent(player2, "Sprouting Phytohydra")
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, phytohydraId);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount()).isEqualTo(1);
        assertThat(countPermanents(player2, "Sprouting Phytohydra")).isEqualTo(2);

        harness.activateAbility(player1, 0, null, phytohydraId);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount()).isEqualTo(2);
        assertThat(countPermanents(player2, "Sprouting Phytohydra")).isEqualTo(2);
        assertThat(findPermanents(player2, "Sprouting Phytohydra"))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Fully prevented damage does not trigger a copy")
    void fullyPreventedDamageDoesNotTrigger() {
        UUID phytohydraId = addPhytohydraAndSealOfFire();
        findPermanent(player2, "Sprouting Phytohydra").setDamagePreventionShield(2);

        harness.activateAbility(player1, 0, null, phytohydraId);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(tokenCount()).isZero();
        assertThat(countPermanents(player2, "Sprouting Phytohydra")).isEqualTo(1);
    }

    private UUID addPhytohydraAndSealOfFire() {
        harness.addToBattlefield(player2, new SproutingPhytohydra());
        harness.addToBattlefield(player1, new SealOfFire());
        harness.addToBattlefield(player1, new SealOfFire());
        return harness.getPermanentId(player2, "Sprouting Phytohydra");
    }

    private long tokenCount() {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
