package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RepentantBlacksmith;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhostlyFlameTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        return card;
    }

    private void addGhostlyFlame() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GhostlyFlame()));
    }

    @Test
    @DisplayName("Protection from red no longer prevents combat damage from a red creature")
    void redCombatDamageIsNotPreventedByProtectionFromRed() {
        Permanent attacker = new Permanent(createCreature("Big Goblin", 3, 3, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new RepentantBlacksmith());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addGhostlyFlame();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> "Repentant Blacksmith".equals(p.getCard().getName()));
    }

    @Test
    @DisplayName("Protection from green still prevents combat damage from a green creature")
    void greenCombatDamageIsStillPrevented() {
        Permanent attacker = new Permanent(createCreature("Big Bear", 3, 3, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Card wall = createCreature("Ward Wall", 0, 1, CardColor.WHITE);
        wall.addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
        Permanent blocker = new Permanent(wall);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addGhostlyFlame();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> "Ward Wall".equals(p.getCard().getName()));
    }

    @Test
    @DisplayName("Protection from black no longer prevents combat damage from a black creature")
    void blackCombatDamageIsNotPreventedByProtectionFromBlack() {
        Permanent attacker = new Permanent(createCreature("Big Zombie", 3, 3, CardColor.BLACK));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Card wall = createCreature("Ward Wall", 0, 1, CardColor.WHITE);
        wall.addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        Permanent blocker = new Permanent(wall);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addGhostlyFlame();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> "Ward Wall".equals(p.getCard().getName()));
    }

    @Test
    @DisplayName("Protection from red still stops a red spell from targeting")
    void redSpellStillCannotTarget() {
        Permanent blacksmith = new Permanent(new RepentantBlacksmith());
        blacksmith.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blacksmith);

        // A second, legal target keeps the spell playable so the failure is target legality,
        // not an empty playable list.
        Permanent bears = new Permanent(createCreature("Bears", 2, 2, CardColor.GREEN));
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Shock", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        addGhostlyFlame();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, blacksmith.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("A red creature still can't block a creature with protection from red")
    void redCreatureStillCannotBlock() {
        Permanent attacker = new Permanent(new RepentantBlacksmith());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Goblin", 2, 2, CardColor.RED));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addGhostlyFlame();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
