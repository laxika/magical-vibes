package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PollenLullaby.class, Forest.class, KithkinGreatheart.class})
class PollenLullabyTest extends BaseCardTest {

    private void castPollenLullaby() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PollenLullaby()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        // Clash outcome is irrelevant to the prevention clause.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        castPollenLullaby();

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage to players and creatures")
    void preventsCombatDamageToPlayersAndCreatures() {
        Permanent attacker = addCreatureReady(player1, new KithkinGreatheart());
        Permanent blocker = addCreatureReady(player2, new KithkinGreatheart());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        castPollenLullaby();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Winning the clash freezes the opponent's creatures through their next untap step")
    void wonClashFreezesOpponentCreatures() {
        // Kithkin Greatheart's mana value 2 is greater than Forest's mana value 0, so player1 wins.
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new KithkinGreatheart());

        castPollenLullaby();

        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Winning the clash does not affect the caster's own creatures")
    void wonClashLeavesOwnCreaturesAlone() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new KithkinGreatheart());

        castPollenLullaby();

        assertThat(ownCreature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Frozen opponent creature stays tapped through its next untap step")
    void frozenCreatureStaysTapped() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new KithkinGreatheart());
        opponentCreature.tap();

        castPollenLullaby();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);

        advanceToUpkeep(player2);
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Winning the clash also freezes creatures entering before the opponent's next untap step")
    void wonClashAffectsCreatureEnteringBeforeNextUntap() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        castPollenLullaby();

        Permanent laterCreature = addCreatureReady(player2, new KithkinGreatheart());
        laterCreature.tap();

        advanceToUpkeep(player2);
        assertThat(laterCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Winning the clash does not freeze the opponent's noncreatures")
    void wonClashLeavesOpponentNoncreaturesAlone() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castPollenLullaby();

        assertThat(opponentLand.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Losing the clash leaves the opponent's creatures untouched")
    void lostClashDoesNotFreeze() {
        // Forest's mana value 0 is less than Kithkin Greatheart's mana value 2, so player1 loses.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new KithkinGreatheart());

        castPollenLullaby();

        assertThat(opponentCreature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("A mana value tie is not a win, so nothing is frozen")
    void tiedClashDoesNotFreeze() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new KithkinGreatheart());

        castPollenLullaby();

        assertThat(opponentCreature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Prevents damage from an unblocked attacker even after losing the clash")
    void preventsUnblockedCombatDamageAfterLostClash() {
        addCreatureReady(player1, new KithkinGreatheart());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new KithkinGreatheart()));

        castPollenLullaby();
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The untap restriction expires after the opponent's next untap step")
    void untapRestrictionExpiresAfterOneStep() {
        harness.setLibrary(player1, List.of(new KithkinGreatheart()));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent creature = addCreatureReady(player2, new KithkinGreatheart());
        creature.tap();

        castPollenLullaby();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

}
