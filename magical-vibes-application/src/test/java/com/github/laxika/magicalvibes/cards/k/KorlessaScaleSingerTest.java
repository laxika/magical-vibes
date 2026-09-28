package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DragonbornLooter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorlessaScaleSinger.class, DragonbornLooter.class, GrizzlyBears.class})
class KorlessaScaleSingerTest extends BaseCardTest {

    @Test
    void castsDragonFromLibraryTop() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        Card dragon = new DragonbornLooter();
        harness.setLibrary(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragonborn Looter");
    }

    @Test
    void cannotCastNonDragonFromLibraryTop() {
        harness.addToBattlefield(player1, new KorlessaScaleSinger());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }
}
