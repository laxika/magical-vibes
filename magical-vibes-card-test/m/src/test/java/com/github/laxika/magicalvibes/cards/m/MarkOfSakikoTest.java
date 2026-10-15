package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkOfSakiko.class, FrostOgre.class})
class MarkOfSakikoTest extends BaseCardTest {

    @Test
    @DisplayName("Adds green mana equal to combat damage dealt by the enchanted creature")
    void addsManaEqualToCombatDamage() {
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        resolveCombatAndTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The generated green mana survives step transitions until end of turn")
    void generatedManaSurvivesStepTransitions() {
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        resolveCombatAndTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(5);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The generated green mana expires at the end of the turn")
    void generatedManaExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        resolveCombatAndTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(5);

        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The enchanted creature's controller receives the mana")
    void manaGoesToEnchantedCreatureController() {
        Permanent creature = addCreatureReady(player2, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        resolveCombatAndTriggers(player2);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A blocked enchanted creature does not generate mana")
    void blockedCreatureDoesNotGenerateMana() {
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new FrostOgre());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Can be cast on an opponent's creature and attaches on resolution")
    void castsOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new FrostOgre());
        harness.setHand(player1, List.of(new MarkOfSakiko()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mark of Sakiko").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The granted combat damage trigger belongs to the enchanted creature and its controller")
    void grantedTriggerBelongsToCreature() {
        Permanent creature = addCreatureReady(player2, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat(player2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Two copies each generate mana for the same combat damage event")
    void multipleCopiesGenerateManaIndependently() {
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        resolveCombatAndTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Unrelated green mana empties while the generated green mana remains")
    void onlyGeneratedGreenManaPersists() {
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);
        resolveCombatAndTriggers();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    private void attachMarkOfSakiko(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new MarkOfSakiko());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private void resolveCombatAndTriggers() {
        resolveCombatAndTriggers(player1);
    }

    private void resolveCombatAndTriggers(Player activePlayer) {
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            resolveCombat(activePlayer);
            resolveAllTriggers();
        });
    }
}
