package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmissaryOfTheSleeplessTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a Spirit without morbid")
    void doesNotCreateSpiritWithoutMorbid() {
        castEmissary();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates a 1/1 white Spirit with flying when morbid is met")
    void createsSpiritWithMorbid() {
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        castEmissary();
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does not create a Spirit if morbid is lost before the trigger resolves")
    void triggerDoesNothingIfMorbidIsLostBeforeResolution() {
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        castEmissary();
        gd.creatureDeathCountThisTurn.clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void castEmissary() {
        harness.setHand(player1, java.util.List.of(new EmissaryOfTheSleepless()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
