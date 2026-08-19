package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OvikaEnigmaGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates tokens equal to its mana value with haste")
    void noncreatureSpellCreatesManaValueTokensWithHaste() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(3);
        assertThat(findPermanents(player1, "Phyrexian Goblin"))
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue());
    }

    @Test
    @DisplayName("Casting a creature spell does not create tokens")
    void creatureSpellDoesNotCreateTokens() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
    }

    @Test
    @DisplayName("The tokens lose haste at cleanup")
    void tokenHasteWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Phyrexian Goblin");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }
}
