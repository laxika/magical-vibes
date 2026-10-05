package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GryffRider;
import com.github.laxika.magicalvibes.cards.k.KnightOfGrace;
import com.github.laxika.magicalvibes.cards.k.KnightOfMalice;
import com.github.laxika.magicalvibes.cards.t.TwinbladeGeist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OdricBloodCursed.class, GryffRider.class, TwinbladeGeist.class,
        KnightOfGrace.class, KnightOfMalice.class})
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

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {
            "FLYING", "FIRST_STRIKE", "DOUBLE_STRIKE", "DEATHTOUCH", "HASTE", "HEXPROOF",
            "INDESTRUCTIBLE", "LIFELINK", "MENACE", "REACH", "TRAMPLE", "VIGILANCE"
    })
    @DisplayName("Each listed ability independently contributes one Blood token")
    void countsEachListedAbility(Keyword ability) {
        addCreature(player1, "Creature with one ability", ability);
        harness.enterBattlefieldAndReturn(player1, new OdricBloodCursed());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Odric counts duplicate flying once and double strike separately")
    void countsDistinctAbilitiesOnRealCreatures() {
        harness.addToBattlefield(player1, new GryffRider());
        harness.addToBattlefield(player1, new GryffRider());
        harness.addToBattlefield(player1, new TwinbladeGeist());
        harness.castFromHand(player1, new OdricBloodCursed(), "{1}{R}{W}");

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger still counts current creatures after Odric leaves the battlefield")
    void resolvesAfterOdricLeaves() {
        harness.addToBattlefield(player1, new GryffRider());
        var odric = harness.enterBattlefieldAndReturn(player1, new OdricBloodCursed());
        gd.playerBattlefields.get(player1.getId()).remove(odric);
        gd.playerGraveyards.get(player1.getId()).add(odric.getCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("An ability on a creature that left before resolution no longer contributes")
    void ignoresCreatureThatLeftBeforeResolution() {
        var rider = harness.addToBattlefieldAndReturn(player1, new GryffRider());
        harness.enterBattlefieldAndReturn(player1, new OdricBloodCursed());
        gd.playerBattlefields.get(player1.getId()).remove(rider);
        gd.playerGraveyards.get(player1.getId()).add(rider.getCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    @DisplayName("Different hexproof variants count once alongside first strike")
    void countsHexproofVariantsOnce() {
        harness.addToBattlefield(player1, new KnightOfGrace());
        harness.addToBattlefield(player1, new KnightOfMalice());
        harness.enterBattlefieldAndReturn(player1, new OdricBloodCursed());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
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
