package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SergeantAtArmsTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 2/3, no tokens created")
    void castWithoutKickerNoTokens() {
        harness.setHand(player1, List.of(new SergeantAtArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2); // generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sergeant-at-Arms"));
        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();
        // No tokens created — only Sergeant-at-Arms on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setHand(player1, List.of(new SergeantAtArms()));
        harness.addMana(player1, ManaColor.WHITE, 2); // {W} base + {W} kicker
        harness.addMana(player1, ManaColor.RED, 4); // 2 generic base + 2 generic kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        // Creature entered the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sergeant-at-Arms"));
        // ETB trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — creates two 1/1 white Soldier creature tokens")
    void castWithKickerCreatesTwoTokens() {
        harness.setHand(player1, List.of(new SergeantAtArms()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Sergeant-at-Arms + 2 tokens = 3 permanents
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);

        // Verify the tokens
        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .toList();
        assertThat(tokens).hasSize(2);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(token.getCard().isToken()).isTrue();
    }
}
