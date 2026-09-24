package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.p.PentadPrism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringerOfTheRedDawn.class, DrossCrocodile.class, PentadPrism.class})
class BringerOfTheRedDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for the five-color alternate cost")
    void castsForAlternateCost() {
        harness.setHand(player1, List.of(new BringerOfTheRedDawn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bringer of the Red Dawn");
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BringerOfTheRedDawn());
        Permanent blocker = addCreatureReady(player2, new DrossCrocodile());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Accepting the upkeep ability untaps, steals, and grants haste to the target")
    void acceptsUpkeepAbility() {
        harness.addToBattlefield(player1, new BringerOfTheRedDawn());
        Permanent target = addCreatureReady(player2, new DrossCrocodile());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("The upkeep ability can target a creature its controller controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new BringerOfTheRedDawn());
        Permanent target = addCreatureReady(player1, new DrossCrocodile());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep ability leaves the target unchanged")
    void decliningUpkeepAbilityDoesNothing() {
        harness.addToBattlefield(player1, new BringerOfTheRedDawn());
        Permanent target = addCreatureReady(player2, new DrossCrocodile());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        harness.addToBattlefield(player1, new BringerOfTheRedDawn());
        Permanent target = addCreatureReady(player2, new DrossCrocodile());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void doesNotTriggerAtOpponentUpkeep() {
        harness.addToBattlefield(player1, new BringerOfTheRedDawn());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The upkeep ability does nothing if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new BringerOfTheRedDawn());
        Permanent target = addCreatureReady(player2, new DrossCrocodile());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The upkeep ability can target creatures but not noncreature permanents")
    void onlyCreaturesAreLegalTargets() {
        harness.addToBattlefield(player1, new BringerOfTheRedDawn());
        Permanent target = addCreatureReady(player2, new DrossCrocodile());
        Permanent noncreature = new Permanent(new PentadPrism());
        gd.playerBattlefields.get(player2.getId()).add(noncreature);

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(noncreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
