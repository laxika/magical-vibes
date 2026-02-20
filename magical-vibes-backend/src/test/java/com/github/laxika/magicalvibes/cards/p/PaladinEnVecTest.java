package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaladinEnVecTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

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
        card.setNeedsTarget(true);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Paladin en-Vec has correct card properties")
    void hasCorrectProperties() {
        PaladinEnVec card = new PaladinEnVec();

        assertThat(card.getName()).isEqualTo("Paladin en-Vec");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.KNIGHT);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(ProtectionFromColorsEffect.class);

        ProtectionFromColorsEffect protection = (ProtectionFromColorsEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(protection.colors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Paladin en-Vec puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Paladin en-Vec");
    }

    @Test
    @DisplayName("Cannot cast Paladin en-Vec without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts Paladin en-Vec on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paladin en-Vec"));
    }

    @Test
    @DisplayName("Paladin en-Vec enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Paladin en-Vec"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("First strike kills 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = new Permanent(new PaladinEnVec());
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

        // First strike kills Bears before it deals damage; Paladin survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paladin en-Vec"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Black creature cannot block Paladin en-Vec")
    void blackCreatureCannotBlockPaladin() {
        Permanent attacker = new Permanent(new PaladinEnVec());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Black Knight", 2, 2, CardColor.BLACK));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Red creature cannot block Paladin en-Vec")
    void redCreatureCannotBlockPaladin() {
        Permanent attacker = new Permanent(new PaladinEnVec());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Goblin Raider", 2, 1, CardColor.RED));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Paladin en-Vec")
    void greenCreatureCanBlockPaladin() {
        Permanent attacker = new Permanent(new PaladinEnVec());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("Paladin takes no combat damage from black creature")
    void paladinTakesNoDamageFromBlack() {
        // Black 3/3 attacker, Paladin as blocker
        Permanent attacker = new Permanent(createCreature("Black Knight", 3, 3, CardColor.BLACK));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new PaladinEnVec());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Paladin has first strike: deals 2 to Black Knight (3/3 survives)
        // Black Knight's 3 regular damage to Paladin is prevented (protection)
        // Both survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paladin en-Vec"));
    }

    @Test
    @DisplayName("Paladin takes no combat damage from red creature")
    void paladinTakesNoDamageFromRed() {
        // Red 3/3 attacker, Paladin as blocker
        Permanent attacker = new Permanent(createCreature("Fire Elemental", 3, 3, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new PaladinEnVec());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Both survive: Paladin deals 2 first strike (2 < 3), red creature's 3 damage is prevented
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fire Elemental"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paladin en-Vec"));
    }

    @Test
    @DisplayName("Paladin takes normal combat damage from green creature")
    void paladinTakesNormalDamageFromGreen() {
        // Green 3/3 attacker, Paladin as blocker
        Permanent attacker = new Permanent(createCreature("Big Green", 3, 3, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new PaladinEnVec());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Paladin deals 2 first strike (2 < 3, green survives)
        // Green deals 3 regular damage (3 >= 2, Paladin dies — no protection from green)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Big Green"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Paladin en-Vec"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Paladin en-Vec"));
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent paladin = new Permanent(new PaladinEnVec());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(paladin);

        harness.setHand(player1, List.of(createTargetedInstant("Dark Banishing", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, paladin.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent paladin = new Permanent(new PaladinEnVec());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(paladin);

        harness.setHand(player1, List.of(createTargetedInstant("Lightning Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, paladin.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent paladin = new Permanent(new PaladinEnVec());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, paladin.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bandage");
    }

    // ===== Protection - aura enchantment =====

    @Test
    @DisplayName("Cannot be enchanted by black aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent paladin = new Permanent(new PaladinEnVec());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(paladin);

        Card blackAura = new Card();
        blackAura.setName("Unholy Strength");
        blackAura.setType(CardType.ENCHANTMENT);
        blackAura.setManaCost("{B}");
        blackAura.setColor(CardColor.BLACK);
        blackAura.setSubtypes(List.of(CardSubtype.AURA));
        blackAura.setNeedsTarget(true);
        harness.setHand(player1, List.of(blackAura));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, paladin.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be enchanted by white aura (Holy Strength)")
    void canBeEnchantedByWhiteAura() {
        Permanent paladin = new Permanent(new PaladinEnVec());
        paladin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(paladin);

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, paladin.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Strength");
    }
}

