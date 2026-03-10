package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackKnightTest extends BaseCardTest {

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
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Black Knight has protection from white static effect")
    void hasCorrectProperties() {
        BlackKnight card = new BlackKnight();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(ProtectionFromColorsEffect.class);

        ProtectionFromColorsEffect protection = (ProtectionFromColorsEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(protection.colors()).containsExactly(CardColor.WHITE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Black Knight puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BlackKnight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Black Knight");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts Black Knight on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BlackKnight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("First strike kills 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = new Permanent(new BlackKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // First strike kills Bears before it deals damage; Black Knight survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("White creature cannot block Black Knight")
    void whiteCreatureCannotBlockBlackKnight() {
        Permanent attacker = new Permanent(new BlackKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("White Knight", 2, 2, CardColor.WHITE));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Black Knight")
    void greenCreatureCanBlockBlackKnight() {
        Permanent attacker = new Permanent(new BlackKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("Black Knight takes no combat damage from white creature")
    void takesNoDamageFromWhiteCreature() {
        Permanent attacker = new Permanent(createCreature("White Knight", 3, 3, CardColor.WHITE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new BlackKnight());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Black Knight has first strike: deals 2 to White Knight (3/3 survives)
        // White Knight's 3 regular damage to Black Knight is prevented (protection)
        // Both survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("White Knight"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));
    }

    @Test
    @DisplayName("Black Knight takes normal combat damage from green creature")
    void takesNormalDamageFromGreenCreature() {
        Permanent attacker = new Permanent(createCreature("Big Green", 3, 3, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new BlackKnight());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Black Knight deals 2 first strike (2 < 3, green survives)
        // Green deals 3 regular damage (3 >= 2, Black Knight dies — no protection from green)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Big Green"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Black Knight"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Black Knight"));
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent knight = new Permanent(new BlackKnight());
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(knight);

        harness.setHand(player1, List.of(createTargetedInstant("White Removal", CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, knight.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by black instant")
    void canBeTargetedByBlackInstant() {
        Permanent knight = new Permanent(new BlackKnight());
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(knight);

        harness.setHand(player1, List.of(createTargetedInstant("Dark Blast", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, knight.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dark Blast");
    }

    // ===== Protection - aura enchantment =====

    @Test
    @DisplayName("Cannot be enchanted by white aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent knight = new Permanent(new BlackKnight());
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(knight);

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, knight.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be enchanted by black aura")
    void canBeEnchantedByBlackAura() {
        Permanent knight = new Permanent(new BlackKnight());
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(knight);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, knight.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unholy Strength");
    }
}
