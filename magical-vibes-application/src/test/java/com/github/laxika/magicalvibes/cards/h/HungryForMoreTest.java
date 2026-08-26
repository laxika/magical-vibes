package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HungryForMore.class)
class HungryForMoreTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/1 black and red Vampire token with trample, lifelink, and haste")
    void createsVampireToken() {
        castFromHand();

        Permanent vampire = findPermanent(player1, "Vampire");
        assertThat(vampire.getCard().getPower()).isEqualTo(3);
        assertThat(vampire.getCard().getToughness()).isEqualTo(1);
        assertThat(vampire.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(vampire.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(vampire.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(vampire.getCard().getKeywords())
                .contains(Keyword.TRAMPLE, Keyword.LIFELINK, Keyword.HASTE);
    }

    @Test
    @DisplayName("Sacrifices the Vampire token at the beginning of the next end step")
    void sacrificesVampireTokenAtNextEndStep() {
        castFromHand();
        harness.assertOnBattlefield(player1, "Vampire");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vampire");
    }

    @Test
    @DisplayName("Flashback creates the token and exiles Hungry for More")
    void flashbackCreatesTokenAndExilesSpell() {
        HungryForMore hungryForMore = new HungryForMore();
        harness.setGraveyard(player1, List.of(hungryForMore));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hungryForMore);
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new HungryForMore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
