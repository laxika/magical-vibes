package com.github.laxika.magicalvibes.cards.p;

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
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianCrusaderTest extends BaseCardTest {

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
    @DisplayName("Phyrexian Crusader has protection from red and white")
    void hasCorrectProtection() {
        PhyrexianCrusader card = new PhyrexianCrusader();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(ProtectionFromColorsEffect.class);

        ProtectionFromColorsEffect protection = (ProtectionFromColorsEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(protection.colors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Phyrexian Crusader puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PhyrexianCrusader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Crusader");
    }

    @Test
    @DisplayName("Resolving puts Phyrexian Crusader on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PhyrexianCrusader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Phyrexian Crusader");
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Red creature cannot block Phyrexian Crusader")
    void redCreatureCannotBlock() {
        Permanent attacker = new Permanent(new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Goblin Raider", 2, 2, CardColor.RED));
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
    @DisplayName("White creature cannot block Phyrexian Crusader")
    void whiteCreatureCannotBlock() {
        Permanent attacker = new Permanent(new PhyrexianCrusader());
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
    @DisplayName("Black creature can block Phyrexian Crusader")
    void blackCreatureCanBlock() {
        Permanent attacker = new Permanent(new PhyrexianCrusader());
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

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("Phyrexian Crusader takes no combat damage from red creature")
    void takesNoDamageFromRed() {
        Permanent attacker = new Permanent(createCreature("Fire Elemental", 3, 3, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new PhyrexianCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Crusader deals 2 first strike as -1/-1 counters (Fire Elemental becomes 1/1)
        // Fire Elemental's 3 damage to Crusader is prevented (protection from red)
        // Regular damage: Fire Elemental deals 0 (prevention), Crusader has no regular damage (first strike only)
        // Fire Elemental survives with -1/-1 counters
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Crusader"));
    }

    @Test
    @DisplayName("Phyrexian Crusader takes no combat damage from white creature")
    void takesNoDamageFromWhite() {
        Permanent attacker = new Permanent(createCreature("Serra Angel", 4, 4, CardColor.WHITE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new PhyrexianCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Serra Angel's 4 damage to Crusader is prevented (protection from white)
        // Crusader survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Crusader"));
    }

    @Test
    @DisplayName("Phyrexian Crusader takes normal combat damage from black creature")
    void takesNormalDamageFromBlack() {
        Permanent attacker = new Permanent(createCreature("Sengir Vampire", 4, 4, CardColor.BLACK));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new PhyrexianCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Crusader deals 2 first strike as -1/-1 counters (Sengir becomes 2/2)
        // Regular damage: Sengir deals 4 damage to Crusader (kills 2/2), Crusader doesn't deal again (first strike only)
        // Crusader dies from black damage (no protection)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Crusader"));
        harness.assertInGraveyard(player2, "Phyrexian Crusader");
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent crusader = new Permanent(new PhyrexianCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        harness.setHand(player1, List.of(createTargetedInstant("Lightning Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be targeted by white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent crusader = new Permanent(new PhyrexianCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        harness.setHand(player1, List.of(createTargetedInstant("Swords to Plowshares", CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be targeted by black instant")
    void canBeTargetedByBlackInstant() {
        Permanent crusader = new Permanent(new PhyrexianCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crusader);

        harness.setHand(player1, List.of(createTargetedInstant("Dark Banishing", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, crusader.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dark Banishing");
    }

    // ===== Protection - aura enchantment =====

    @Test
    @DisplayName("Cannot be enchanted by white aura")
    void cannotBeEnchantedByWhiteAura() {
        Permanent crusader = new Permanent(new PhyrexianCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(crusader);

        Card whiteAura = new Card();
        whiteAura.setName("Holy Strength");
        whiteAura.setType(CardType.ENCHANTMENT);
        whiteAura.setManaCost("{W}");
        whiteAura.setColor(CardColor.WHITE);
        whiteAura.setSubtypes(List.of(CardSubtype.AURA));
        harness.setHand(player1, List.of(whiteAura));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crusader.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Can be enchanted by black aura (Unholy Strength)")
    void canBeEnchantedByBlackAura() {
        Permanent crusader = new Permanent(new PhyrexianCrusader());
        crusader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crusader);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, crusader.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unholy Strength");
    }

    // ===== Infect - poison counters to player =====

    @Test
    @DisplayName("Unblocked Phyrexian Crusader deals poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent attacker = new Permanent(new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (2)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    // ===== Infect - -1/-1 counters to creatures =====

    @Test
    @DisplayName("Phyrexian Crusader deals -1/-1 counters to blocker instead of regular damage")
    void dealsMinusCountersToBlocker() {
        // Use a 4/4 black creature so it survives and we can check counters
        Permanent blocker = new Permanent(createCreature("Sengir Vampire", 4, 4, CardColor.BLACK));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent attacker = new Permanent(new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Crusader deals 2 first strike as -1/-1 counters; 4/4 becomes 2/2
        // Sengir deals 4 damage to Crusader (kills it since no protection from black)
        harness.assertInGraveyard(player1, "Phyrexian Crusader");

        // Sengir should have 2 -1/-1 counters
        Permanent survivingBlocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sengir Vampire"))
                .findFirst().orElseThrow();
        assertThat(survivingBlocker.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    // ===== First strike - kills blocker before it deals damage =====

    @Test
    @DisplayName("First strike with infect kills 2/2 blocker with -1/-1 counters before it deals damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = new Permanent(new PhyrexianCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Green 2/2 — not protected color, can block
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // First strike: Crusader deals 2 infect damage as -1/-1 counters → Grizzly Bears becomes 0/0 → dies
        // Grizzly Bears never gets to deal regular damage back
        harness.assertOnBattlefield(player1, "Phyrexian Crusader");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
