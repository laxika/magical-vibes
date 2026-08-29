package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SadisticSkymarcherTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Vampire in hand it costs {2}{B} plus the additional {1}")
    void requiresExtraOneWithoutVampire() {
        harness.setHand(player1, List.of(new SadisticSkymarcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {1} can be paid with mana when no Vampire is revealed")
    void payTheOneWithMana() {
        SadisticSkymarcher skymarcher = new SadisticSkymarcher();
        harness.setHand(player1, List.of(skymarcher));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {2} + {1}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(skymarcher.getId()));
    }

    @Test
    @DisplayName("Revealing a Vampire card from hand lets it be cast for just {2}{B}")
    void revealVampireAvoidsTheOne() {
        SadisticSkymarcher skymarcher = new SadisticSkymarcher();
        CaptivatingVampire vampireInHand = new CaptivatingVampire();
        harness.setHand(player1, List.of(skymarcher, vampireInHand));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(skymarcher.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(vampireInHand.getId()));
    }
}
