package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkymarcherAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("It does not have flying without the city's blessing")
    void noFlyingWithoutBlessing() {
        Permanent aspirant = addCreatureReady(player1, new SkymarcherAspirant());

        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Permanent ascend grants the city's blessing when the tenth permanent enters")
    void gainsFlyingWhenTenthPermanentEnters() {
        Permanent aspirant = harness.addToBattlefieldAndReturn(player1, new SkymarcherAspirant());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting it as the tenth permanent gives its controller the city's blessing")
    void ascendsWhenItEntersAsTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SkymarcherAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent aspirant = findPermanent(player1, "Skymarcher Aspirant");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, aspirant, Keyword.FLYING)).isTrue();
    }
}
