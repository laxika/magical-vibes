package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinalShowdown.class, GrizzlyBears.class, SerraAngel.class})
class FinalShowdownTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode removes abilities from every creature")
    void removesAbilitiesFromAllCreatures() {
        Permanent ownAngel = addCreatureReady(player1, new SerraAngel());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isTrue();

        cast(new int[]{0}, 2);

        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The second mode chooses your creature and grants indestructible")
    void grantsIndestructibleToChosenCreature() {
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new SerraAngel());

        cast(new int[]{1}, 2);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gqs.hasKeyword(gd, chosen, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Spree pays each selected mode and later grants survive earlier ability loss")
    void paysAndResolvesMultipleModes() {
        Permanent chosen = addCreatureReady(player1, new SerraAngel());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0, 1}, 3);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The third mode destroys all creatures")
    void destroysAllCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{2}, 6);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void cast(int[] modes, int whiteMana) {
        harness.setHand(player1, List.of(new FinalShowdown()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);
        harness.castModalInstantWithModes(player1, 0, 1, 3, modes, List.of());
        harness.passBothPriorities();
    }
}
