package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CeaselessConflict.class, GrizzlyBears.class})
class CeaselessConflictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and creates one Spirit for each nontoken creature the caster controlled")
    void destroysCreaturesAndCountsOnlyNontokenCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, spiritToken());
        addCreatureReady(player2, new GrizzlyBears());

        castCeaselessConflict();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).allSatisfy(this::assertSpirit);
    }

    @Test
    @DisplayName("Regeneration and indestructible creatures survive without creating Spirits")
    void creaturesNotDestroyedDoNotCount() {
        Permanent indestructible = addCreatureReady(player1, new GrizzlyBears());
        indestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent regenerating = addCreatureReady(player2, new GrizzlyBears());
        regenerating.setRegenerationShield(1);

        castCeaselessConflict();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(indestructible);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(regenerating);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    private void castCeaselessConflict() {
        harness.castFromHand(player1, new CeaselessConflict(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    private void assertSpirit(Permanent spirit) {
        assertThat(spirit.getCard().getPower()).isEqualTo(3);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(spirit.getCard().isToken()).isTrue();
    }

    private static Card spiritToken() {
        Card token = new Card();
        token.setName("Spirit Token");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.WHITE);
        token.setColors(List.of(CardColor.RED, CardColor.WHITE));
        token.setSubtypes(List.of(CardSubtype.SPIRIT));
        token.setPower(3);
        token.setToughness(2);
        token.setToken(true);
        return token;
    }
}
