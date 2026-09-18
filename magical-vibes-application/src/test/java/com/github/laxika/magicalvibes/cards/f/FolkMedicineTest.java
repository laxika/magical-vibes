package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FolkMedicine.class, BorderPatrol.class, KrosanVerge.class})
class FolkMedicineTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each creature you control")
    void gainsLifeForEachCreatureYouControl() {
        harness.addToBattlefield(player1, new BorderPatrol());
        harness.addToBattlefield(player1, new BorderPatrol());
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.addToBattlefield(player2, new BorderPatrol());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new FolkMedicine(), "{2}{G}");

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Flashback gains life and exiles Folk Medicine")
    void flashbackGainsLifeAndExilesSpell() {
        harness.addToBattlefield(player1, new BorderPatrol());
        harness.addToBattlefield(player1, new BorderPatrol());
        harness.setLife(player1, 10);
        FolkMedicine spell = new FolkMedicine();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }
}
