package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MaulSplicer.class, Xenograft.class})
class MaulSplicerTest extends BaseCardTest {

    

    @Test
    @DisplayName("ETB creates two 3/3 colorless Phyrexian Golem artifact creature tokens")
    void etbCreatesTwoGolemTokens() {
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // Maul Splicer + 2 Golem tokens

        List<Permanent> golemTokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(golemTokens).hasSize(2);

        for (Permanent golem : golemTokens) {
            assertThat(golem.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
            assertThat(golem.getCard().getColor()).isNull();
            assertThat(golem.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(golem.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(golem.getEffectivePower()).isEqualTo(3);
            assertThat(golem.getEffectiveToughness()).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Golem tokens have trample from Maul Splicer's static ability")
    void golemTokensHaveTrample() {
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> golemTokens = findPermanents(player1, "Phyrexian Golem");

        assertThat(golemTokens).hasSize(2);
        for (Permanent golem : golemTokens) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Maul Splicer itself does not have trample (not a Golem)")
    void maulSplicerDoesNotHaveTrample() {
        harness.addToBattlefield(player1, new MaulSplicer());

        Permanent maulSplicer = findPermanent(player1, "Maul Splicer");

        assertThat(gqs.hasKeyword(gd, maulSplicer, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is granted to all Golems you control, not just the tokens")
    void grantsTrampleToOtherGolems() {
        harness.addToBattlefield(player1, new MaulSplicer());

        // Cast a second Maul Splicer to get Golem tokens on the battlefield
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // Leave only the first Splicer to grant trample to the second one's tokens.
        gd.playerBattlefields.get(player1.getId()).remove(1);

        List<Permanent> golems = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .toList();

        assertThat(golems).isNotEmpty();
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent's Golems do not get trample")
    void opponentGolemsDoNotGetTrample() {
        harness.addToBattlefield(player1, new MaulSplicer());

        // Put a Golem on the opponent's battlefield via a second Maul Splicer
        harness.setHand(player2, List.of(new MaulSplicer()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent p2MaulSplicer = findPermanent(player2, "Maul Splicer");
        gd.playerBattlefields.get(player2.getId()).remove(p2MaulSplicer);

        List<Permanent> golems = findPermanents(player2, "Phyrexian Golem");
        assertThat(golems).hasSize(2);
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
        }
    }

    @Test
    @DisplayName("Trample is lost when Maul Splicer leaves the battlefield")
    void trampleLostWhenMaulSplicerLeaves() {
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        // Verify golem has trample
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.TRAMPLE)).isTrue();

        // Remove Maul Splicer from battlefield
        Permanent maulSplicer = findPermanent(player1, "Maul Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(maulSplicer);

        // Golem should no longer have trample
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("ETB still creates both tokens when Maul Splicer leaves before the trigger resolves")
    void tokensCreatedAfterSourceLeaves() {
        harness.setHand(player1, List.of(new MaulSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent splicer = findPermanent(player1, "Maul Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(splicer);
        resolveAllTriggers();

        List<Permanent> golems = findPermanents(player1, "Phyrexian Golem");
        assertThat(golems).hasSize(2);
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
        }
    }

    @Test
    @DisplayName("Maul Splicer receives trample when Xenograft makes it a Golem")
    void gainsTrampleWhenItBecomesAGolem() {
        harness.addToBattlefield(player1, new MaulSplicer());
        harness.castFromHand(player1, new Xenograft(), "{4}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOLEM");

        Permanent splicer = findPermanent(player1, "Maul Splicer");
        assertThat(gqs.hasKeyword(gd, splicer, Keyword.TRAMPLE)).isTrue();
    }
}
