package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdventOfTheWurmTest extends BaseCardTest {

    private static final int GREEN_MANA_NEEDED = 3;

    @Test
    @DisplayName("Creates a 5/5 green Wurm token with trample")
    void createsWurmToken() {
        harness.setHand(player1, List.of(new AdventOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, GREEN_MANA_NEEDED);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Wurm");

        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(harness.getGameData(), token, Keyword.TRAMPLE)).isTrue();
    }
}
