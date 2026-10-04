package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenArcher;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SeasClaim;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GracefulAntelope.class, Mountain.class, AvenArcher.class, SeasClaim.class})
class GracefulAntelopeTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may make a target land a Plains until the Antelope leaves")
    void combatDamageChangesLandUntilSourceLeaves() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.PLAINS);

        gd.playerBattlefields.get(player1.getId()).remove(antelope);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Declining the combat trigger leaves the target land unchanged")
    void decliningCombatTriggerDoesNothing() {
        attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("The combat trigger cannot choose a non-land permanent")
    void cannotChooseNonLandPermanent() {
        attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = addCreatureReady(player2, new AvenArcher());

        resolveCombatAndTrigger();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Combat damage dealt to a creature does not trigger the ability")
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent blocker = addCreatureReady(player2, new AvenArcher());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(antelope))));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The accepted effect does nothing if the Antelope leaves before resolution")
    void sourceLeavingBeforeResolutionPreventsLandChange() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        gd.playerBattlefields.get(player1.getId()).remove(antelope);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("The trigger can change your own land and its mana production")
    void ownLandProducesWhiteInsteadOfRed() {
        attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mountain));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A land changed into a Plains makes the Antelope unblockable")
    void changedLandEnablesPlainswalk() {
        Permanent antelope = attackWithAntelope(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent blocker = addCreatureReady(player2, new AvenArcher());

        prepareDeclareBlockers();
        antelope.setAttacking(true);
        antelope.setAttackTarget(player2.getId());
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(antelope)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A resolving trigger overrides a land Aura attached after the Antelope entered")
    void resolvedLandChangeUsesResolutionTimestamp() {
        Permanent antelope = harness.enterBattlefieldAndReturn(player1, new GracefulAntelope());
        antelope.setSummoningSick(false);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SeasClaim()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, mountain.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.ISLAND);

        antelope.setAttacking(true);
        antelope.setAttackTarget(player2.getId());
        resolveCombatAndTrigger();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.PLAINS);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, antelope));
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.ISLAND);
    }

    private Permanent attackWithAntelope(Player player) {
        Permanent antelope = addCreatureReady(player, new GracefulAntelope());
        antelope.setAttacking(true);
        antelope.setAttackTarget(player2.getId());
        return antelope;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
