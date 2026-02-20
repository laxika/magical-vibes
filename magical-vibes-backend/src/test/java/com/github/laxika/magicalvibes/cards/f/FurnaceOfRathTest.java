package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FurnaceOfRathTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameService gs;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        gs = harness.getGameService();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Furnace of Rath has correct card properties")
    void hasCorrectProperties() {
        FurnaceOfRath card = new FurnaceOfRath();

        assertThat(card.getName()).isEqualTo("Furnace of Rath");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(DoubleDamageEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Furnace of Rath puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FurnaceOfRath()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Furnace of Rath");
    }

    @Test
    @DisplayName("Resolving Furnace of Rath puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new FurnaceOfRath()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Furnace of Rath"));
    }

    // ===== Doubles spell damage to player =====

    @Test
    @DisplayName("Doubles X damage from Blaze to a player")
    void doublesSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Doubles damage from activated ability to a player")
    void doublesActivatedAbilityDamageToPlayer() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        Permanent invoker = addReadyInvoker(player1);
        harness.addMana(player1, ManaColor.RED, 8);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        // 5 damage doubled to 10
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    // ===== Doubles spell damage to creatures =====

    @Test
    @DisplayName("Doubled spell damage destroys a creature that would survive base damage")
    void doublesSpellDamageToCreature() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castSorcery(player1, 0, 2, serraId);
        harness.passBothPriorities();

        // 2 damage doubled to 4 — kills Serra Angel (4/4)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Creature survives doubled damage less than toughness")
    void creatureSurvivesDoubledDamageLessThanToughness() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castSorcery(player1, 0, 1, serraId);
        harness.passBothPriorities();

        // 1 damage doubled to 2 — Serra Angel (4/4) survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra Angel"));
    }

    // ===== Doubles combat damage =====

    @Test
    @DisplayName("Doubles unblocked combat damage to player")
    void doublesUnblockedCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FurnaceOfRath());

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(1)); // bear is at index 1 (Furnace at 0)

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubled combat damage kills blocker that would survive base damage")
    void doublesCombatDamageKillsBlocker() {
        harness.addToBattlefield(player1, new FurnaceOfRath());

        // 2/2 attacker
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // 4/4 blocker — base 2 damage wouldn't kill it, but doubled 4 does
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        // Serra Angel (4/4) takes 2*2=4 doubled damage — exactly lethal
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));

        // Grizzly Bears (2/2) takes 4*2=8 doubled damage — also dies
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Doubles first-strike combat damage =====

    @Test
    @DisplayName("Doubles first-strike combat damage to player")
    void doublesFirstStrikeDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FurnaceOfRath());
        addReadyCreature(player1, new BenalishKnight()); // 2/2 first strike

        declareAttackers(player1, List.of(1)); // Furnace at 0, knight at 1

        // 2 first strike damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubled first strike kills blocker before it deals regular damage")
    void doubledFirstStrikeKillsBlockerBeforeRegularDamage() {
        harness.addToBattlefield(player1, new FurnaceOfRath());

        // 2/2 first strike attacker
        Permanent attacker = new Permanent(new BenalishKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // 3/3 blocker — base 2 first-strike damage < 3, but doubled 4 >= 3
        GrizzlyBears creature3_3 = new GrizzlyBears();
        creature3_3.setPower(3);
        creature3_3.setToughness(3);
        Permanent blocker = new Permanent(creature3_3);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        // Doubled first strike (4) kills 3/3 before it can deal regular damage
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Knight survives since blocker died in first strike phase
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Benalish Knight"));
    }

    @Test
    @DisplayName("Blocker survives doubled first strike — no double-doubling across phases")
    void blockerSurvivesDoubledFirstStrike() {
        harness.addToBattlefield(player1, new FurnaceOfRath());

        // 2/2 first strike attacker
        Permanent attacker = new Permanent(new BenalishKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // 3/5 blocker — doubled first strike deals 4 < 5, survives
        GrizzlyBears creature3_5 = new GrizzlyBears();
        creature3_5.setPower(3);
        creature3_5.setToughness(5);
        Permanent blocker = new Permanent(creature3_5);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        // Phase 1: 2 * 2 = 4 < 5, blocker survives first strike
        // Phase 2: blocker deals 3 * 2 = 6 >= 2, knight dies
        // Blocker total damage = 4 (FS attacker doesn't deal phase 2 damage), 4 < 5, survives
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Benalish Knight"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Doubles double-strike combat damage =====

    @Test
    @DisplayName("Doubles both phases of double-strike damage to player")
    void doublesBothPhasesOfDoubleStrikeDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FurnaceOfRath());
        addReadyCreature(player1, new SkyhunterSkirmisher()); // 1/1 double strike flying

        declareAttackers(player1, List.of(1)); // Furnace at 0, skirmisher at 1

        // Phase 1: 1 * 2 = 2, Phase 2: 1 * 2 = 2, Total: 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubled double-strike kills blocker that would survive without Furnace")
    void doubledDoubleStrikeKillsBlockerThatWouldSurvive() {
        harness.addToBattlefield(player1, new FurnaceOfRath());

        // 1/1 double strike flying attacker
        Permanent attacker = new Permanent(new SkyhunterSkirmisher());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // 3/3 with reach — without Furnace: 1+1=2 < 3, survives. With Furnace: 2+2=4 >= 3, dies.
        GrizzlyBears creature3_3 = new GrizzlyBears();
        creature3_3.setPower(3);
        creature3_3.setToughness(3);
        creature3_3.setKeywords(Set.of(Keyword.REACH));
        Permanent blocker = new Permanent(creature3_3);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        // Phase 1: 1*2=2 < 3, survives. Phase 2: 1*2=2, total 4 >= 3, dies.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blocker survives doubled double-strike — no double-doubling across phases")
    void blockerSurvivesDoubledDoubleStrike() {
        harness.addToBattlefield(player1, new FurnaceOfRath());

        // 1/1 double strike flying attacker
        Permanent attacker = new Permanent(new SkyhunterSkirmisher());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // 3/5 with reach — total doubled damage 2+2=4 < 5, survives
        GrizzlyBears creature3_5 = new GrizzlyBears();
        creature3_5.setPower(3);
        creature3_5.setToughness(5);
        creature3_5.setKeywords(Set.of(Keyword.REACH));
        Permanent blocker = new Permanent(creature3_5);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        // Phase 1: 1*2=2 < 5, survives. Phase 2: 1*2=2, total 4 < 5, survives.
        // Blocker deals 3*2=6 >= 1, Skirmisher dies.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Skyhunter Skirmisher"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Affects all players symmetrically =====

    @Test
    @DisplayName("Doubles damage dealt to Furnace controller too")
    void doublesDamageToController() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 3, player1.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6 — even against Furnace's controller
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Opponent's Furnace also doubles your damage")
    void opponentsFurnaceDoublesYourDamage() {
        harness.addToBattlefield(player2, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    // ===== Multiple Furnaces stack multiplicatively =====

    @Test
    @DisplayName("Two Furnaces quadruple damage")
    void twoFurnacesQuadrupleDamage() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.addToBattlefield(player2, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage * 2 * 2 = 12
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Two Furnaces quadruple combat damage")
    void twoFurnacesQuadrupleCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.addToBattlefield(player2, new FurnaceOfRath());

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(1)); // bear is at index 1 (Furnace at 0; other Furnace is on player2's side)

        // 2 combat damage * 4 = 8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    // ===== Effect removed when Furnace leaves =====

    @Test
    @DisplayName("Removing Furnace from battlefield stops doubling")
    void removingFurnaceStopsDoubling() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setLife(player2, 20);

        // Deal doubled damage first
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Remove Furnace from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Furnace of Rath"));

        // Deal damage again without Furnace
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        // 2 damage (not doubled), life goes from 16 to 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    // ===== No damage means no doubling =====

    @Test
    @DisplayName("X=0 deals no damage even with Furnace")
    void zeroDamageNotDoubled() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent addReadyInvoker(Player player) {
        FlamewaveInvoker card = new FlamewaveInvoker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        gs.declareAttackers(gd, player, attackerIndices);
    }
}

