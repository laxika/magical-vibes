package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAllCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlindingFogTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Blinding Fog has correct effects")
    void hasCorrectEffects() {
        BlindingFog card = new BlindingFog();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(PreventAllDamageToAllCreaturesEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(GrantKeywordEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Blinding Fog puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BlindingFog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blinding Fog");
    }

    @Test
    @DisplayName("Cannot cast Blinding Fog without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new BlindingFog()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolution — damage prevention =====

    @Test
    @DisplayName("Resolving Blinding Fog sets preventAllDamageToAllCreatures flag")
    void resolvingSetsPreventionFlag() {
        harness.setHand(player1, List.of(new BlindingFog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.preventAllDamageToAllCreatures).isTrue();
    }

    @Test
    @DisplayName("Blinding Fog goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new BlindingFog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blinding Fog"));
    }

    // ===== Prevents combat damage to controller's creature =====

    @Test
    @DisplayName("Prevents combat damage to controller's blocking creature")
    void preventsCombatDamageToOwnBlockingCreature() {
        harness.getGameData().preventAllDamageToAllCreatures = true;

        Permanent attacker = new Permanent(createCreature("Big Bear", 5, 5));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Grizzly Bears (2/2) survives because combat damage is prevented
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Prevents combat damage to opponent's creature too =====

    @Test
    @DisplayName("Prevents combat damage to opponent's creature as well")
    void preventsCombatDamageToOpponentsCreature() {
        harness.getGameData().preventAllDamageToAllCreatures = true;

        Permanent attacker = new Permanent(createCreature("Big Bear", 5, 5));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Big Bear (5/5) also survives — damage to ALL creatures is prevented
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Big Bear"));
    }

    // ===== Does NOT prevent damage to players =====

    @Test
    @DisplayName("Does not prevent combat damage to players")
    void doesNotPreventCombatDamageToPlayers() {
        harness.setLife(player2, 20);
        harness.getGameData().preventAllDamageToAllCreatures = true;

        Permanent attacker = new Permanent(createCreature("Bear", 3, 3));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player takes damage — Blinding Fog only prevents creature damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Prevents spell damage to creatures =====

    @Test
    @DisplayName("Prevents spell damage to controller's creature")
    void preventsSpellDamageToOwnCreature() {
        harness.getGameData().preventAllDamageToAllCreatures = true;

        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Grizzly Bears (2/2) survives Shock because damage to creatures is prevented
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Prevents spell damage to opponent's creature")
    void preventsSpellDamageToOpponentsCreature() {
        harness.getGameData().preventAllDamageToAllCreatures = true;

        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Grizzly Bears survives — damage to all creatures is prevented
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Hexproof =====

    @Test
    @DisplayName("Resolving Blinding Fog grants hexproof to controller's creatures")
    void grantsHexproofToOwnCreatures() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new BlindingFog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Blinding Fog does not grant hexproof to opponent's creatures")
    void doesNotGrantHexproofToOpponentsCreatures() {
        Permanent ownCreature = new Permanent(new GrizzlyBears());
        ownCreature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(ownCreature);

        Permanent opponentCreature = new Permanent(createCreature("Opponent Bear", 2, 2));
        opponentCreature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.setHand(player1, List.of(new BlindingFog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
        assertThat(opponentCreature.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    // ===== Clears at end of turn =====

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        harness.getGameData().preventAllDamageToAllCreatures = true;

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.preventAllDamageToAllCreatures).isFalse();
    }
}
