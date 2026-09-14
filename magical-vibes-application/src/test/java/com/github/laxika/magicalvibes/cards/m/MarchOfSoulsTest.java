package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchOfSouls.class, GrizzlyBears.class, Plains.class})
class MarchOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and gives each creature's controller a Spirit per creature destroyed")
    void destroysCreaturesAndCreatesSpiritsForTheirControllers() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        castMarchOfSouls();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains);
        harness.assertNotInGraveyard(player1, "Plains");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).hasSize(2);
        assertThat(findPermanents(player1, "Spirit")).allSatisfy(this::assertSpirit);
        assertThat(findPermanents(player2, "Spirit")).allSatisfy(this::assertSpirit);
    }

    @Test
    @DisplayName("Does not create a Spirit for a creature that cannot be destroyed")
    void indestructibleCreaturesSurviveWithoutCreatingSpirits() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent indestructible = addCreatureReady(player2, new GrizzlyBears());
        indestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castMarchOfSouls();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructible);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Destroys creatures despite regeneration shields")
    void destroysCreaturesDespiteRegenerationShields() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);

        castMarchOfSouls();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
    }

    private void castMarchOfSouls() {
        harness.castFromHand(player1, new MarchOfSouls(), "{4}{W}");
        harness.passBothPriorities();
    }

    private void assertSpirit(Permanent spirit) {
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.getCard().isToken()).isTrue();
    }
}
