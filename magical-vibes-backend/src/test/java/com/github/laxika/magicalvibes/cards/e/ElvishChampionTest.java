package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishChampionTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Elvish Champion has correct card properties")
    void hasCorrectProperties() {
        ElvishChampion card = new ElvishChampion();

        assertThat(card.getName()).isEqualTo("Elvish Champion");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ELF);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(BoostCreaturesBySubtypeEffect.class);

        BoostCreaturesBySubtypeEffect effect = (BoostCreaturesBySubtypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.affectedSubtypes()).containsExactly(CardSubtype.ELF);
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).containsExactly(Keyword.FORESTWALK);
    }

    @Test
    @DisplayName("Casting Elvish Champion puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ElvishChampion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Elvish Champion");
    }

    @Test
    @DisplayName("Resolving puts Elvish Champion onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ElvishChampion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elvish Champion"));
    }

    @Test
    @DisplayName("Other Elf creatures get +1/+1 and forestwalk")
    void buffsOtherElves() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new ElvishChampion());

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Elvish Champion does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ElvishChampion());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Champion"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, champion, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Elf creatures")
    void doesNotBuffNonElves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ElvishChampion());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Elf creatures too")
    void buffsOpponentElves() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent opponentElf = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Two Elvish Champions buff each other")
    void twoChampionsBuffEachOther() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player1, new ElvishChampion());

        List<Permanent> champions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Champion"))
                .toList();

        assertThat(champions).hasSize(2);
        for (Permanent champion : champions) {
            assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, champion, Keyword.FORESTWALK)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Elvish Champions give +2/+2 to other Elves")
    void twoChampionsStackBonuses() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Elvish Champion leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elvish Champion"));

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Elvish Champion resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new ElvishChampion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player1, new LlanowarElves());

        Permanent elf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        elf.setPowerModifier(elf.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(7);

        elf.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Elf with forestwalk cannot be blocked when defender controls a Forest")
    void forestwalkPreventsBlockingWhenDefenderControlsForest() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player2, new Forest());

        Permanent elfAttacker = new Permanent(new LlanowarElves());
        elfAttacker.setSummoningSick(false);
        elfAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(elfAttacker);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(elfAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Elf with forestwalk can be blocked when defender does not control a Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        harness.addToBattlefield(player1, new ElvishChampion());

        Permanent elfAttacker = new Permanent(new LlanowarElves());
        elfAttacker.setSummoningSick(false);
        elfAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(elfAttacker);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(elfAttacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Elvish Champion itself does not have forestwalk when alone")
    void championDoesNotHaveForestwalkItself() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player2, new Forest());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Champion"))
                .findFirst().orElseThrow();
        champion.setSummoningSick(false);
        champion.setAttacking(true);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(champion);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk is lost when Elvish Champion leaves the battlefield")
    void forestwalkLostWhenChampionLeaves() {
        harness.addToBattlefield(player1, new ElvishChampion());
        harness.addToBattlefield(player2, new Forest());

        Permanent elfAttacker = new Permanent(new LlanowarElves());
        elfAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elfAttacker);

        assertThat(gqs.hasKeyword(gd, elfAttacker, Keyword.FORESTWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elvish Champion"));

        assertThat(gqs.hasKeyword(gd, elfAttacker, Keyword.FORESTWALK)).isFalse();
    }
}
