package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MatterReshaper;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaxosOfMeletis.class, Forest.class, TravelingPhilosopher.class, NessianCourser.class})
class DaxosOfMeletisTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the damaged player's top card and gains its mana value in life")
    void combatDamageExilesTopCardAndGainsLife() {
        addAttackingDaxos(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card topCard = new TravelingPhilosopher();
        harness.setLibrary(player2, List.of(topCard, new TravelingPhilosopher()));

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("The exiled spell can be cast with mana of any color until end of turn")
    void castsExiledSpellWithManaOfAnyColor() {
        addAttackingDaxos(player1);
        Card topCard = new TravelingPhilosopher();
        harness.setLibrary(player2, List.of(topCard, new TravelingPhilosopher()));

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Traveling Philosopher");
    }

    @Test
    @DisplayName("A land exiled by the trigger cannot be played")
    void doesNotGrantLandPlayPermission() {
        addAttackingDaxos(player1);
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard, new TravelingPhilosopher()));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        harness.assertLife(player1, 20);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Daxos cannot be blocked by a creature with power 3 or greater")
    void cannotBeBlockedByPowerThreeOrGreater() {
        Permanent daxos = addAttackingDaxos(player1);
        Permanent blocker = addCreatureReady(player2, new NessianCourser());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int daxosIndex = gd.playerBattlefields.get(player1.getId()).indexOf(daxos);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, daxosIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureWithPowerTwoCanBlock() {
        Permanent daxos = addAttackingDaxos(player1);
        Permanent blocker = addCreatureReady(player2, new TravelingPhilosopher());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(daxos))));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    void emptyLibraryDoesNotGainLifeOrGrantPermission() {
        addAttackingDaxos(player1);
        harness.setLibrary(player2, List.of());
        harness.setLife(player1, 15);

        resolveCombatAndTrigger();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 18);
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void creatureStillRequiresSorceryTiming() {
        addAttackingDaxos(player1);
        Card topCard = new TravelingPhilosopher();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void permissionExpiresAtEndOfTurnButCardRemainsExiled() {
        addAttackingDaxos(player1);
        Card topCard = new TravelingPhilosopher();
        harness.setLibrary(player2, List.of(topCard, new Forest(), new Forest()));
        resolveCombatAndTrigger();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cardOwnerDoesNotReceiveCastingPermission() {
        addAttackingDaxos(player1);
        Card topCard = new TravelingPhilosopher();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        resolveCombatAndTrigger();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    void colorlessManaCanPayColoredRequirements() {
        addAttackingDaxos(player1);
        Card topCard = new TravelingPhilosopher();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Traveling Philosopher");
    }

    @Test
    @CardUsed(MatterReshaper.class)
    void coloredManaCannotPayRequiredColorlessMana() {
        addAttackingDaxos(player1);
        Card topCard = new MatterReshaper();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @CardUsed(MatterReshaper.class)
    void requiredColorlessManaCanBePaidWithColorlessMana() {
        addAttackingDaxos(player1);
        Card topCard = new MatterReshaper();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Matter Reshaper");
    }

    private Permanent addAttackingDaxos(Player player) {
        Permanent daxos = addCreatureReady(player, new DaxosOfMeletis());
        daxos.setAttacking(true);
        return daxos;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
