package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OdricBloodCursed.class})
class OdricBloodCursedTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Blood token for each distinct listed ability among your creatures")
    void createsBloodTokensForDistinctAbilities() {
        harness.enterBattlefieldAndReturn(player1, new OdricBloodCursed());
        addCreature(player1, "Flying First Striker", Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.DEFENDER);
        addCreature(player1, "Deathtouch First Striker", Keyword.DEATHTOUCH, Keyword.FIRST_STRIKE);
        addCreature(player1, "Vigilant Creature", Keyword.VIGILANCE);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts abilities present when the enters-the-battlefield trigger resolves")
    void countsAtResolution() {
        harness.enterBattlefieldAndReturn(player1, new OdricBloodCursed());
        addCreature(player1, "Flying Creature", Keyword.FLYING);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Ignores abilities on opposing creatures and unlisted abilities")
    void ignoresOpposingAndUnlistedAbilities() {
        harness.enterBattlefieldAndReturn(player1, new OdricBloodCursed());
        addCreature(player1, "Defender", Keyword.DEFENDER);
        addCreature(player2, "Flying Creature", Keyword.FLYING);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    private void addCreature(com.github.laxika.magicalvibes.model.Player player, String name,
                             Keyword... abilities) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(abilities));
        addCreatureReady(player, card);
    }
}
