package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpareFromEvilTest extends BaseCardTest {

    private static Card createHumanCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        return card;
    }

    private static Card createNonHumanCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(CardSubtype.BEAST));
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Spare from Evil has correct effect")
    void hasCorrectEffect() {
        SpareFromEvil card = new SpareFromEvil();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect.class);
        GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect effect =
                (GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.excludedSubtype()).isEqualTo(CardSubtype.HUMAN);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Spare from Evil puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    // ===== Resolution grants protection =====

    @Test
    @DisplayName("Resolving grants protection from non-Human creatures to all controlled creatures")
    void resolvingGrantsProtection() {
        harness.addToBattlefield(player1, createHumanCreature("Elite Vanguard", 2, 1));
        harness.addToBattlefield(player1, createNonHumanCreature("Bear", 2, 2));

        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (gqs.isCreature(gd, p)) {
                assertThat(p.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
            }
        }
    }

    // ===== Protection prevents blocking by non-Human creatures =====

    @Test
    @DisplayName("Non-Human creature cannot block creature with protection from non-Human creatures")
    void nonHumanCannotBlock() {
        Permanent attacker = new Permanent(createHumanCreature("Elite Vanguard", 2, 1));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn().add(CardSubtype.HUMAN);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createNonHumanCreature("Beast", 3, 3));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Blocker should not be able to block the protected attacker
        assertThat(gqs.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Human creature CAN block creature with protection from non-Human creatures")
    void humanCanStillBlock() {
        Permanent attacker = new Permanent(createHumanCreature("Elite Vanguard", 2, 1));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn().add(CardSubtype.HUMAN);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createHumanCreature("Human Blocker", 2, 2));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Human blocker should be able to block the protected attacker
        assertThat(gqs.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    // ===== Protection prevents combat damage from non-Human creatures =====

    @Test
    @DisplayName("Combat damage from non-Human blocker is prevented by protection")
    void combatDamageFromNonHumanPrevented() {
        Permanent attacker = new Permanent(createHumanCreature("Elite Vanguard", 2, 1));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn().add(CardSubtype.HUMAN);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createNonHumanCreature("Big Beast", 5, 5));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Elite Vanguard (2/1) should survive because 5 damage from non-Human is prevented
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
    }

    // ===== Protection clears at end of turn =====

    @Test
    @DisplayName("Protection is cleared at end of turn via resetModifiers")
    void protectionClearedAtEndOfTurn() {
        Permanent creature = new Permanent(createHumanCreature("Soldier", 2, 2));
        creature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn().add(CardSubtype.HUMAN);

        assertThat(creature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).isNotEmpty();

        creature.resetModifiers();

        assertThat(creature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).isEmpty();
    }

    // ===== Does not affect opponent's creatures =====

    @Test
    @DisplayName("Does not grant protection to opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, createHumanCreature("Own Soldier", 2, 2));
        harness.addToBattlefield(player2, createHumanCreature("Opponent Soldier", 2, 2));

        harness.setHand(player1, List.of(new SpareFromEvil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent ownCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Own Soldier")).findFirst().orElseThrow();
        Permanent opponentCreature = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Opponent Soldier")).findFirst().orElseThrow();

        assertThat(ownCreature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).contains(CardSubtype.HUMAN);
        assertThat(opponentCreature.getProtectionFromNonSubtypeCreaturesUntilEndOfTurn()).isEmpty();
    }
}
