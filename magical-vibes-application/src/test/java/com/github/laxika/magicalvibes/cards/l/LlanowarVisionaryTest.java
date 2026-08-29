package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LlanowarVisionaryTest extends BaseCardTest {

    @Test
    void entersAndDrawsACard() {
        harness.setHand(player1, List.of(new LlanowarVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Visionary");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void tapAbilityAddsGreenMana() {
        Permanent visionary = addCreatureReady(player1, new LlanowarVisionary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(visionary.isTapped()).isTrue();
    }
}
