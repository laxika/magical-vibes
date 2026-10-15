package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FolkMedicine.class, KrosanVerge.class, SuntailHawk.class})
class FolkMedicineTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each creature you control")
    void gainsLifeForEachCreatureYouControl() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new FolkMedicine(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Counts creatures when Folk Medicine resolves")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new FolkMedicine(), "{2}{G}");
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Flashback gains life and exiles Folk Medicine")
    void flashbackGainsLifeAndExilesSpell() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setLife(player1, 10);
        FolkMedicine spell = new FolkMedicine();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Gains no life with no creatures, even when the opponent controls creatures")
    void gainsNoLifeWithoutControlledCreatures() {
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        FolkMedicine spell = new FolkMedicine();

        harness.castFromHand(player1, spell, "{2}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Folk Medicine");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("The second player gains life for only their own creatures")
    void secondPlayerGainsLifeForOwnCreatures() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.castFromHand(player2, new FolkMedicine(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Flashback with no creatures still exiles the spell")
    void flashbackWithNoCreaturesStillExilesSpell() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setLife(player1, 10);
        FolkMedicine spell = new FolkMedicine();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertLife(player1, 10);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Folk Medicine");
    }
}
