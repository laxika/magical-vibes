package com.github.laxika.magicalvibes.cards.h;
import java.util.Set;
import com.github.laxika.magicalvibes.model.CardType;

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

@CardUsed(HauntedCadaver.class)
class HauntedCadaverTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may sacrifice Haunted Cadaver and make the player discard three cards")
    void combatDamageMaySacrificeAndDiscardThree() {
        harness.setHand(player2, List.of(new HauntedCadaver(), new HauntedCadaver(), new HauntedCadaver()));
        addAttacker();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Haunted Cadaver");
        harness.assertInGraveyard(player1, "Haunted Cadaver");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Combat damage uses the damaged player without asking for a target")
    void combatDamageUsesDamagedPlayerWithoutTargetChoice() {
        addAttacker();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining the may ability keeps Haunted Cadaver on the battlefield")
    void decliningMayKeepsCadaver() {
        harness.setHand(player2, List.of(new HauntedCadaver()));
        addAttacker();

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Haunted Cadaver");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A blocked Haunted Cadaver does not trigger")
    void blockedCadaverDoesNotTrigger() {
        addAttacker();
        Permanent blocker = addCreatureReady(player2, new HauntedCadaver());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Haunted Cadaver");
    }

    @Test
    @DisplayName("A Haunted Cadaver that leaves before the choice resolves cannot discard")
    void leavingBeforeMayResolutionPreventsDiscard() {
        harness.setHand(player2, List.of(new HauntedCadaver(), new HauntedCadaver(), new HauntedCadaver()));
        Permanent attacker = addAttacker();

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new HauntedCadaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent cadaver = findPermanent(player1, "Haunted Cadaver");
        assertThat(cadaver.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cadaver));
        harness.passBothPriorities();

        assertThat(cadaver.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("A player with fewer than three cards discards their entire hand")
    void discardsAllCardsFromShortHand() {
        harness.setHand(player2, List.of(new HauntedCadaver(), new HauntedCadaver()));
        addAttacker();

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Haunted Cadaver");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Haunted Cadaver can be sacrificed even when the damaged player has no cards")
    void canSacrificeAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        addAttacker();

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Haunted Cadaver");
        harness.assertInGraveyard(player1, "Haunted Cadaver");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Face-down Haunted Cadaver deals combat damage without its printed trigger")
    void faceDownCombatDamageDoesNotTrigger() {
        harness.setHand(player2, List.of(new HauntedCadaver()));
        Permanent attacker = addAttacker();
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The second player's Cadaver makes the first player discard")
    void secondPlayerCadaverDiscardsFromDamagedPlayer() {
        harness.setHand(player1, List.of(new HauntedCadaver()));
        Permanent attacker = addCreatureReady(player2, new HauntedCadaver());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Haunted Cadaver");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new HauntedCadaver());
        attacker.setAttacking(true);
        return attacker;
    }

}
