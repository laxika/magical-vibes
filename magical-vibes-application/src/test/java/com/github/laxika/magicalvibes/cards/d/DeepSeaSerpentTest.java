package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepSeaSerpent.class, Island.class})
class DeepSeaSerpentTest extends BaseCardTest {

    // ===== Attack restriction =====

    @Test
    @DisplayName("Deep-Sea Serpent can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new DeepSeaSerpent());
        declareAttackers(List.of(0));

        // Combat auto-advances; 5/5 unblocked deals 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deep-Sea Serpent cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new DeepSeaSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deep-Sea Serpent cannot attack if defender controls only a changeling creature")
    void cannotAttackWhenDefenderOnlyControlsChangelingCreature() {
        Card changeling = new Card();
        changeling.setName("Test Changeling");
        changeling.setType(CardType.CREATURE);
        changeling.setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        changeling.setKeywords(Set.of(Keyword.CHANGELING));
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(changeling));

        addCreatureReady(player1, new DeepSeaSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Unblocked Deep-Sea Serpent deals 5 damage to defending player")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent serpent = addCreatureReady(player1, new DeepSeaSerpent());
        serpent.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
