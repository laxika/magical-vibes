package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeaMonsterTest {

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

    // ===== Card properties =====

    @Test
    @DisplayName("Sea Monster has correct card properties")
    void hasCorrectProperties() {
        SeaMonster card = new SeaMonster();

        assertThat(card.getName()).isEqualTo("Sea Monster");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{4}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(6);
        assertThat(card.getToughness()).isEqualTo(6);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SERPENT);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CantAttackUnlessDefenderControlsMatchingPermanentEffect.class);
        CantAttackUnlessDefenderControlsMatchingPermanentEffect effect =
                (CantAttackUnlessDefenderControlsMatchingPermanentEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.defenderPermanentPredicate()).isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.ISLAND));
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sea Monster puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SeaMonster()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sea Monster");
    }

    @Test
    @DisplayName("Resolving puts Sea Monster onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SeaMonster()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sea Monster"));
    }

    @Test
    @DisplayName("Sea Monster enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new SeaMonster()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sea Monster"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Sea Monster can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        Permanent seaPerm = new Permanent(new SeaMonster());
        seaPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seaPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Combat auto-advances; verify attack went through by checking damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Sea Monster cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        Permanent seaPerm = new Permanent(new SeaMonster());
        seaPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seaPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sea Monster cannot attack if defender controls only a changeling creature")
    void cannotAttackWhenDefenderOnlyControlsChangelingCreature() {
        Card changeling = new Card();
        changeling.setName("Test Changeling");
        changeling.setType(CardType.CREATURE);
        changeling.setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        changeling.setKeywords(Set.of(Keyword.CHANGELING));
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(changeling));

        Permanent seaPerm = new Permanent(new SeaMonster());
        seaPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seaPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Unblocked Sea Monster deals 6 damage to defending player")
    void dealsSixDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent seaPerm = new Permanent(new SeaMonster());
        seaPerm.setSummoningSick(false);
        seaPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(seaPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}


