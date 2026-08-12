package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Darksteel Gargoyle has flying and indestructible")
    void hasFlyingAndIndestructible() {
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Darksteel Gargoyle survives a destroy effect")
    void survivesDestroyEffect() {
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, gargoyle.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        harness.assertNotInGraveyard(player2, "Darksteel Gargoyle");
    }
}
