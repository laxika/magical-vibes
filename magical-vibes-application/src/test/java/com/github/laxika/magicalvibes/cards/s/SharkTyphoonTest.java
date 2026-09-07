package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharkTyphoon.class, Divination.class, GrizzlyBears.class})
class SharkTyphoonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates a flying Shark with that spell's mana value")
    void noncreatureSpellCreatesManaValueShark() {
        harness.addToBattlefield(player1, new SharkTyphoon());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Shark")).isEqualTo(1);
        Permanent shark = findPermanent(player1, "Shark");
        assertThat(gqs.getEffectivePower(gd, shark)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shark)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, shark, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Shark")
    void creatureSpellDoesNotCreateShark() {
        harness.addToBattlefield(player1, new SharkTyphoon());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Shark")).isZero();
    }

    @Test
    @DisplayName("Cycling creates a Shark using the chosen X and draws a card")
    void cyclingCreatesChosenSizeSharkAndDraws() {
        harness.setHand(player1, List.of(new SharkTyphoon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null, 2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Shark")).isEqualTo(1);
        Permanent shark = findPermanent(player1, "Shark");
        assertThat(gqs.getEffectivePower(gd, shark)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shark)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, shark, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Shark Typhoon");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
