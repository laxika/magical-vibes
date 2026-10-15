package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DireFleetDaredevil;
import com.github.laxika.magicalvibes.cards.g.GleamingBarrier;
import com.github.laxika.magicalvibes.cards.v.VaultOfCatlacan;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormTheVault.class, VaultOfCatlacan.class, SunSentinel.class, GleamingBarrier.class,
        DireFleetDaredevil.class})
class StormTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure when multiple creatures deal combat damage to a player")
    void createsOneTreasureForOneCombatDamageEvent() {
        addReadyStorm(player1);
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        resolveCombatDamage();

        assertThat(treasureCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforms at the end step with exactly five artifacts")
    void transformsWithFiveArtifacts() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 5; i++) {
            addArtifact(player1);
        }

        resolveEndStep(player1);

        assertThat(storm.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at the end step with only four artifacts")
    void doesNotTransformWithFourArtifacts() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 4; i++) {
            addArtifact(player1);
        }

        resolveEndStep(player1);

        assertThat(storm.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Vault of Catlacan adds one mana of the chosen color")
    void vaultAddsChosenColor() {
        Permanent vault = addTransformedVault(player1);

        harness.activateAbility(player1, indexOf(player1, vault), 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vault of Catlacan adds blue mana for each artifact you control")
    void vaultAddsBlueForArtifacts() {
        Permanent vault = addTransformedVault(player1);
        addArtifact(player1);
        addArtifact(player1);
        addArtifact(player1);

        harness.activateAbility(player1, indexOf(player1, vault), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    void firstStrikeAndRegularDamageEachCreateTreasure() {
        addReadyStorm(player1);
        addReadyAttacker(player1);
        Permanent firstStriker = harness.addToBattlefieldAndReturn(player1, new DireFleetDaredevil());
        firstStriker.setSummoningSick(false);
        firstStriker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(treasureCount(player1)).isEqualTo(2);
    }

    @Test
    void doesNotCreateTreasureWithoutCombatDamage() {
        addReadyStorm(player1);

        resolveCombatDamage();

        assertThat(treasureCount(player1)).isZero();
    }

    @Test
    void opponentsCreaturesDoNotTriggerStorm() {
        addReadyStorm(player2);
        addReadyAttacker(player1);

        resolveCombatDamage();

        assertThat(treasureCount(player2)).isZero();
    }

    @Test
    void treasureTriggerResolvesAfterStormLeavesBattlefield() {
        Permanent storm = addReadyStorm(player1);
        addReadyAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(storm);

        harness.passBothPriorities();

        assertThat(treasureCount(player1)).isEqualTo(1);
    }

    @Test
    void createdTreasureCanBeSacrificedForMana() {
        addReadyStorm(player1);
        addReadyAttacker(player1);
        resolveCombatDamage();
        Permanent treasure = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(perm -> perm.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, indexOf(player1, treasure), 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(treasureCount(player1)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void doesNotTransformOnOpponentsEndStep() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 5; i++) {
            addArtifact(player1);
        }

        resolveEndStep(player2);

        assertThat(storm.isTransformed()).isFalse();
    }

    @Test
    void opponentsArtifactsDoNotMeetTransformCondition() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 4; i++) {
            addArtifact(player1);
        }
        addArtifact(player2);

        resolveEndStep(player1);

        assertThat(storm.isTransformed()).isFalse();
    }

    @Test
    void losingFifthArtifactBeforeResolutionPreventsTransformation() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 4; i++) {
            addArtifact(player1);
        }
        Permanent fifthArtifact = addArtifact(player1);
        beginEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(fifthArtifact);

        harness.passBothPriorities();

        assertThat(storm.isTransformed()).isFalse();
    }

    @Test
    void gainingFifthArtifactAfterEndStepBeginsDoesNotTriggerTransformation() {
        Permanent storm = addReadyStorm(player1);
        for (int i = 0; i < 4; i++) {
            addArtifact(player1);
        }
        beginEndStep(player1);
        assertThat(gd.stack).isEmpty();

        addArtifact(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(storm.isTransformed()).isFalse();
    }

    @Test
    void transformedVaultCanImmediatelyProduceManaAndDoesNotTransformBack() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new StormTheVault());
        for (int i = 0; i < 5; i++) {
            addArtifact(player1);
        }
        resolveEndStep(player1);
        assertThat(storm.isTransformed()).isTrue();

        harness.activateAbility(player1, indexOf(player1, storm), 1, null, null);

        assertThat(storm.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        resolveEndStep(player1);
        assertThat(storm.isTransformed()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "GREEN"})
    void vaultCanProduceEachOtherColor(ManaColor color) {
        Permanent vault = addTransformedVault(player1);

        harness.activateAbility(player1, indexOf(player1, vault), 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blueAbilityTapsVaultEvenWithNoControlledArtifacts() {
        Permanent vault = addTransformedVault(player1);
        addArtifact(player2);

        harness.activateAbility(player1, indexOf(player1, vault), 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyStorm(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new StormTheVault());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addTransformedVault(Player player) {
        StormTheVault card = new StormTheVault();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private void addReadyAttacker(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SunSentinel());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GleamingBarrier());
    }

    private long treasureCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(perm -> perm.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .count();
    }

    private void resolveCombatDamage() {
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveEndStep(Player activePlayer) {
        beginEndStep(activePlayer);
        harness.passBothPriorities();
    }

    private void beginEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
