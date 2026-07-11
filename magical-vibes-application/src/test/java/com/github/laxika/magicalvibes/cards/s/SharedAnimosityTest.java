package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SharedAnimosityTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacker gets +1/+0 per other attacker sharing a creature type")
    void boostScalesWithSharedTypeAttackers() {
        addSharedAnimosity(player1);
        Permanent goblin1 = addCreature(player1, List.of(CardSubtype.GOBLIN));
        Permanent goblin2 = addCreature(player1, List.of(CardSubtype.GOBLIN));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        // Two Goblins attack: each has exactly one other attacker sharing a type -> +1/+0.
        assertThat(goblin1.getPowerModifier()).isEqualTo(1);
        assertThat(goblin1.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin2.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost scales with three attackers sharing a creature type")
    void boostScalesWithThreeSharingAttackers() {
        addSharedAnimosity(player1);
        Permanent goblin1 = addCreature(player1, List.of(CardSubtype.GOBLIN));
        Permanent goblin2 = addCreature(player1, List.of(CardSubtype.GOBLIN));
        Permanent goblin3 = addCreature(player1, List.of(CardSubtype.GOBLIN));

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        // Each Goblin sees two other sharing attackers -> +2/+0.
        assertThat(goblin1.getPowerModifier()).isEqualTo(2);
        assertThat(goblin2.getPowerModifier()).isEqualTo(2);
        assertThat(goblin3.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost when no other attacker shares a creature type")
    void noBoostWithoutSharedType() {
        addSharedAnimosity(player1);
        Permanent goblin = addCreature(player1, List.of(CardSubtype.GOBLIN));
        Permanent elf = addCreature(player1, List.of(CardSubtype.ELF));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(elf.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only attackers sharing a type with the triggering creature are counted")
    void countsOnlySharingAttackers() {
        addSharedAnimosity(player1);
        Permanent goblin1 = addCreature(player1, List.of(CardSubtype.GOBLIN));
        Permanent goblin2 = addCreature(player1, List.of(CardSubtype.GOBLIN));
        Permanent elf = addCreature(player1, List.of(CardSubtype.ELF));

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        // Each Goblin sees one other Goblin (the Elf doesn't count) -> +1/+0.
        assertThat(goblin1.getPowerModifier()).isEqualTo(1);
        assertThat(goblin2.getPowerModifier()).isEqualTo(1);
        // The Elf shares with nobody -> no boost.
        assertThat(elf.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A Changeling attacker shares a creature type with every other attacker")
    void changelingSharesWithEveryAttacker() {
        addSharedAnimosity(player1);
        Permanent changeling = addChangeling(player1);
        Permanent goblin = addCreature(player1, List.of(CardSubtype.GOBLIN));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        // Changeling has every creature type, so it shares with the Goblin and vice versa -> +1 each.
        assertThat(changeling.getPowerModifier()).isEqualTo(1);
        assertThat(goblin.getPowerModifier()).isEqualTo(1);
    }

    // ===== Helpers =====

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    /** Resolve every triggered ability currently on the stack (one Shared Animosity trigger per attacker). */
    private void resolveAllTriggers() {
        int triggers = gd.stack.size();
        for (int i = 0; i < triggers; i++) {
            harness.passBothPriorities();
        }
    }

    private Permanent addSharedAnimosity(Player player) {
        Permanent perm = new Permanent(new SharedAnimosity());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreature(Player player, List<CardSubtype> subtypes) {
        return addCreature(player, subtypes, Set.of());
    }

    private Permanent addChangeling(Player player) {
        // A creature with no printed types but the Changeling keyword — it has every creature type.
        return addCreature(player, List.of(), Set.of(Keyword.CHANGELING));
    }

    private Permanent addCreature(Player player, List<CardSubtype> subtypes, Set<Keyword> keywords) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{R}");
        creature.setColor(CardColor.RED);
        creature.setSubtypes(subtypes);
        creature.setKeywords(keywords);
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
