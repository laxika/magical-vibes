package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunkenPalace.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class SunkenPalaceTest extends BaseCardTest {

    @Test
    void copiesPermanentSpellPaidWithItsMana() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.setGraveyard(player1, graveyardCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
    }

    @Test
    void copiesActivatedAbilityPaidWithItsMana() {
        harness.addToBattlefield(player1, new SunkenPalace());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setGraveyard(player1, graveyardCards());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
    }

    private List<Card> graveyardCards() {
        return List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
    }
}
