package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GitaxianSpellstalker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KhenraSpellspear.class, GitaxianSpellstalker.class, Shock.class, VolcanicSpite.class})
class KhenraSpellspearTest extends BaseCardTest {

    @Test
    void prowessBoostsFrontFaceForNoncreatureSpell() {
        Permanent spellspear = addSpellspear();
        castShock();

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(3);
    }

    @Test
    void transformsWithPhyrexianManaPaidWithLife() {
        Permanent spellspear = addSpellspear();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spellspear.isTransformed()).isTrue();
        assertThat(spellspear.getCard()).isInstanceOf(GitaxianSpellstalker.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void backFaceHasTwoSeparateProwessTriggers() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        castShock();

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(5);
    }

    @Test
    void backFaceWardCountersSpellWithoutPayment() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spellspear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellspear);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addSpellspear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void transformsWithBlueManaWithoutPayingLife() {
        Permanent spellspear = addSpellspear();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(spellspear.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotPayPhyrexianManaWithInsufficientLife() {
        Permanent spellspear = addSpellspear();
        prepareMainPhase(player1);
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(spellspear.isTransformed()).isFalse();
        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTransformWithASpellOnTheStack() {
        Permanent spellspear = addSpellspear();
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(spellspear.isTransformed()).isFalse();
    }

    @Test
    void creatureSpellsDoNotTriggerEitherFacesProwess() {
        Permanent spellspear = addSpellspear();
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new KhenraSpellspear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(2);

        transform(spellspear);
        harness.setHand(player1, List.of(new KhenraSpellspear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(3);
    }

    @Test
    void prowessBonusSurvivesTransformationAndExpiresAtEndOfTurn() {
        Permanent spellspear = addSpellspear();
        castShock();
        transform(spellspear);

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(4);
        castShock();
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(3);
    }

    @Test
    void eachBackFaceProwessTriggerResolvesSeparately() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(4);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    void payingWardAllowsOpponentsLethalSpellToResolve() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new VolcanicSpite()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, spellspear.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spellspear);
        harness.assertInGraveyard(player1, "Khenra Spellspear");
        harness.assertInGraveyard(player2, "Volcanic Spite");
    }

    @Test
    void decliningAffordableWardCountersOpponentsSpell() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new VolcanicSpite()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, spellspear.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellspear);
        harness.assertInGraveyard(player2, "Volcanic Spite");
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(3);
    }

    @Test
    void opponentsNoncreatureSpellsDoNotTriggerEitherFacesProwess() {
        Permanent spellspear = addSpellspear();
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(2);

        transform(spellspear);
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(3);
    }

    @Test
    void controllerTargetingBackFaceTriggersProwessWithoutWard() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, spellspear.getId());
        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellspear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void frontFaceTramplesWithProwessBonus() {
        Permanent spellspear = addCreatureReady(player1, new KhenraSpellspear());
        castShock();
        assertTramplesForOne(spellspear, 17);
    }

    @Test
    void backFaceRetainsTrample() {
        Permanent spellspear = addCreatureReady(player1, new KhenraSpellspear());
        transform(spellspear);
        assertTramplesForOne(spellspear, 19);
    }

    private void assertTramplesForOne(Permanent spellspear, int expectedLife) {
        Permanent blocker = addCreatureReady(player2, new KhenraSpellspear());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, expectedLife);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellspear);
    }

    private Permanent addSpellspear() {
        return harness.addToBattlefieldAndReturn(player1, new KhenraSpellspear());
    }

    private void transform(Permanent spellspear) {
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(spellspear.isTransformed()).isTrue();
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
