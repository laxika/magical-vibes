package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirranCrusaderTest extends BaseCardTest {

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
    @DisplayName("Mirran Crusader has protection from black and green")
    void hasCorrectProperties() {
        MirranCrusader card = new MirranCrusader();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(ProtectionFromColorsEffect.class);

        ProtectionFromColorsEffect protection = (ProtectionFromColorsEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(protection.colors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Mirran Crusader puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mirran Crusader");
    }

    @Test
    @DisplayName("Cannot cast Mirran Crusader without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts Mirran Crusader on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirran Crusader"));
    }

    @Test
    @DisplayName("Mirran Crusader enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mirran Crusader"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Double strike in combat =====

    @Test
    @DisplayName("Double strike kills 2/2 blocker in first strike phase before it deals damage")
    void doubleStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = new Permanent(new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("White Knight", 2, 2, CardColor.WHITE));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // First strike deals 2 damage killing the 2/2 blocker; Crusader survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirran Crusader"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("White Knight"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("White Knight"));
    }

    @Test
    @DisplayName("Double strike deals 4 total damage killing a 4/4 blocker")
    void doubleStrikeKillsLargerBlocker() {
        Permanent attacker = new Permanent(new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Hill Giant", 4, 4, CardColor.WHITE));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // First strike: 2 damage to 4/4 (survives)
        // Regular damage: Crusader deals 2 more (total 4, kills 4/4), Hill Giant deals 4 (kills Crusader)
        // Both die
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mirran Crusader"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mirran Crusader"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Black creature cannot block Mirran Crusader")
    void blackCreatureCannotBlock() {
        Permanent attacker = new Permanent(new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Black Knight", 2, 2, CardColor.BLACK));
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
    @DisplayName("Green creature cannot block Mirran Crusader")
    void greenCreatureCannotBlock() {
        Permanent attacker = new Permanent(new MirranCrusader());
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

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Red creature can block Mirran Crusader")
    void redCreatureCanBlock() {
        Permanent attacker = new Permanent(new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Goblin Raider", 2, 1, CardColor.RED));
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
    @DisplayName("Mirran Crusader takes no combat damage from black creature")
    void takesNoDamageFromBlack() {
        Permanent attacker = new Permanent(createCreature("Black Knight", 3, 3, CardColor.BLACK));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new MirranCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Crusader deals 2 first strike + 2 regular = 4 total (kills 3/3)
        // Black Knight's 3 damage to Crusader is prevented (protection)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Black Knight"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Black Knight"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirran Crusader"));
    }

    @Test
    @DisplayName("Mirran Crusader takes no combat damage from green creature")
    void takesNoDamageFromGreen() {
        Permanent attacker = new Permanent(createCreature("Giant Spider", 3, 3, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new MirranCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Crusader deals 2 first strike + 2 regular = 4 total (kills 3/3)
        // Green creature's 3 damage to Crusader is prevented (protection)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirran Crusader"));
    }

    @Test
    @DisplayName("Mirran Crusader takes normal combat damage from red creature")
    void takesNormalDamageFromRed() {
        Permanent attacker = new Permanent(createCreature("Fire Elemental", 3, 3, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new MirranCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Crusader deals 2 first strike (3/3 survives)
        // Regular damage: Crusader deals 2 more (total 4, kills 3/3), Fire Elemental deals 3 (kills Crusader)
        // Both die
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fire Elemental"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mirran Crusader"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mirran Crusader"));
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent crusader = new Permanent(new MirranCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        // Add valid target so spell is playable
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Dark Banishing", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cannot be targeted by green instant")
    void cannotBeTargetedByGreenInstant() {
        Permanent crusader = new Permanent(new MirranCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        // Add valid target so spell is playable
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Giant Growth", CardColor.GREEN, "{G}")));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent crusader = new Permanent(new MirranCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crusader);

        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, crusader.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bandage");
    }

    // ===== Protection - aura enchantment =====

    @Test
    @DisplayName("Cannot be enchanted by black aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent crusader = new Permanent(new MirranCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        // Add valid target so aura is playable
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Card blackAura = new Card();
        blackAura.setName("Unholy Strength");
        blackAura.setType(CardType.ENCHANTMENT);
        blackAura.setManaCost("{B}");
        blackAura.setColor(CardColor.BLACK);
        blackAura.setSubtypes(List.of(CardSubtype.AURA));
        harness.setHand(player1, List.of(blackAura));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be enchanted by white aura (Holy Strength)")
    void canBeEnchantedByWhiteAura() {
        Permanent crusader = new Permanent(new MirranCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crusader);

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, crusader.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Strength");
    }
}
