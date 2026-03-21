package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JosuVessLichKnightTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 4/5 with menace, no tokens created")
    void castWithoutKickerNoTokens() {
        harness.setHand(player1, List.of(new JosuVessLichKnight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 2); // generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Josu Vess, Lich Knight"));
        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();
        // No tokens created — only Josu Vess on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setHand(player1, List.of(new JosuVessLichKnight()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {B}{B} base + {B} kicker
        harness.addMana(player1, ManaColor.WHITE, 7); // 2 generic base + 5 generic kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Josu Vess, Lich Knight"));
        // ETB trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — creates eight 2/2 black Zombie Knight tokens with menace")
    void castWithKickerCreatesEightTokens() {
        harness.setHand(player1, List.of(new JosuVessLichKnight()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Josu Vess + 8 tokens = 9 permanents
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(9);

        // Verify the tokens
        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Zombie Knight"))
                .toList();
        assertThat(tokens).hasSize(8);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.KNIGHT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE);
        assertThat(token.getCard().isToken()).isTrue();
    }
}
