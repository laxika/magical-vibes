package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.cards.m.MaulSplicer;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeSplicer.class, MaulSplicer.class, Dismember.class, Xenograft.class})
class BladeSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Blade Splicer + Golem token

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");
        assertThat(golemToken.getCard().isToken()).isTrue();
        assertThat(golemToken.getEffectiveColors()).isEmpty();
        assertThat(golemToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golemToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golemToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(golemToken.getEffectivePower()).isEqualTo(3);
        assertThat(golemToken.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Golem token has first strike from Blade Splicer's static ability")
    void golemTokenHasFirstStrike() {
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Blade Splicer itself does not have first strike (not a Golem)")
    void bladeSplicerDoesNotHaveFirstStrike() {
        harness.addToBattlefield(player1, new BladeSplicer());

        Permanent bladeSplicer = findPermanent(player1, "Blade Splicer");

        assertThat(gqs.hasKeyword(gd, bladeSplicer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike is granted to all Golems you control, not just the token")
    void grantsFirstStrikeToOtherGolems() {
        harness.addToBattlefield(player1, new BladeSplicer());

        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> golems = findPermanents(player1, "Phyrexian Golem");

        assertThat(golems).hasSize(2);
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent's Golems do not get first strike")
    void opponentGolemsDoNotGetFirstStrike() {
        harness.addToBattlefield(player1, new BladeSplicer());

        harness.setHand(player2, List.of(new MaulSplicer()));
        harness.addMana(player2, ManaColor.GREEN, 7);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        List<Permanent> golems = findPermanents(player2, "Phyrexian Golem");
        assertThat(golems).hasSize(2);
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        }
    }

    @Test
    @DisplayName("First strike is lost when Blade Splicer leaves the battlefield")
    void firstStrikeLostWhenBladeSplicerLeaves() {
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        // Verify golem has first strike
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FIRST_STRIKE)).isTrue();

        // Remove Blade Splicer from battlefield
        Permanent bladeSplicer = findPermanent(player1, "Blade Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(bladeSplicer);

        // Golem should no longer have first strike
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The enter trigger creates its token even if Blade Splicer dies in response")
    void enterTriggerSurvivesSourceRemoval() {
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent splicer = findPermanent(player1, "Blade Splicer");
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, splicer.getId());
        harness.assertInGraveyard(player1, "Blade Splicer");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(1);
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Phyrexian Golem"), Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Blade Splicer grants itself first strike when Xenograft makes it a Golem")
    void grantsFirstStrikeToItselfWhenItBecomesGolem() {
        harness.addToBattlefield(player1, new BladeSplicer());
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOLEM");
        resolveAllTriggers();

        Permanent splicer = findPermanent(player1, "Blade Splicer");
        assertThat(gqs.hasEffectiveSubtype(gd, splicer, CardSubtype.GOLEM)).isTrue();
        assertThat(gqs.hasKeyword(gd, splicer, Keyword.FIRST_STRIKE)).isTrue();
    }
}
