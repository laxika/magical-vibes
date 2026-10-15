package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LegionConquistador;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindingFog.class, LegionConquistador.class, LightningStrike.class})
class BlindingFogTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blinding Fog puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new BlindingFog(), "{2}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Cannot cast Blinding Fog without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new BlindingFog()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving Blinding Fog sets preventAllDamageToAllCreatures flag")
    void resolvingSetsPreventionFlag() {
        harness.castFromHand(player1, new BlindingFog(), "{2}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.preventAllDamageToAllCreatures).isTrue();
    }

    @Test
    @DisplayName("Blinding Fog goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new BlindingFog(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blinding Fog");
    }

    @Test
    @DisplayName("Prevents lethal combat damage to opposing blocker")
    void preventsCombatDamageToOwnBlockingCreature() {
        resolveFog();

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LegionConquistador());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Legion Conquistador");
    }

    @Test
    @DisplayName("Prevents lethal combat damage to own attacker")
    void preventsCombatDamageToOpponentsCreature() {
        resolveFog();

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LegionConquistador());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Legion Conquistador");
    }

    @Test
    @DisplayName("Does not prevent combat damage to players")
    void doesNotPreventCombatDamageToPlayers() {
        harness.setLife(player2, 20);
        resolveFog();

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents spell damage to opponent's creature")
    void preventsSpellDamageToOpponentCreature() {
        resolveFog();

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LegionConquistador());

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Legion Conquistador");
    }

    @Test
    @DisplayName("Hexproof permits own spells and their damage is prevented")
    void preventsOwnSpellDamageToOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());
        resolveFog();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Legion Conquistador");
    }

    @Test
    @DisplayName("Resolving Blinding Fog grants hexproof to controller's creatures")
    void grantsHexproofToOwnCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());

        harness.castFromHand(player1, new BlindingFog(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Blinding Fog does not grant hexproof to opponent's creatures")
    void doesNotGrantHexproofToOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LegionConquistador());

        harness.castFromHand(player1, new BlindingFog(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
        assertThat(opponentCreature.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        resolveFog();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.preventAllDamageToAllCreatures).isFalse();
    }

    @Test
    void opponentCannotTargetProtectedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());
        resolveFog();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void laterCreatureIsProtectedFromDamageButDoesNotGainHexproof() {
        resolveFog();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Legion Conquistador");
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    void hexproofExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LegionConquistador());
        resolveFog();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Legion Conquistador");
    }

    @Test
    void doesNotPreventSpellDamageToPlayers() {
        harness.setLife(player2, 20);
        resolveFog();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    private void resolveFog() {
        harness.castFromHand(player1, new BlindingFog(), "{2}{G}");
        harness.passBothPriorities();
    }
}
