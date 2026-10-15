package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrostanisSummoner.class, EsixFractalBloom.class})
class TrostanisSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a Knight, a Centaur and a Rhino token")
    void etbCreatesAllThreeTokens() {
        castSummoner();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger

        assertToken(player1, "Knight", 2, 2, Keyword.VIGILANCE);
        assertToken(player1, "Centaur", 3, 3, null);
        assertToken(player1, "Rhino", 4, 4, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Tokens are created under the controller's control only")
    void opponentGetsNoTokens() {
        castSummoner();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tokens(player2, "Knight")).isEmpty();
        assertThat(tokens(player2, "Centaur")).isEmpty();
        assertThat(tokens(player2, "Rhino")).isEmpty();
    }

    private void castSummoner() {
        harness.castFromHand(player1, new TrostanisSummoner(), "{5}{G}{W}");
    }

    @Test
    @DisplayName("Tokens wait for the single enters trigger to resolve")
    void tokensWaitForTriggerResolution() {
        castSummoner();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trostani's Summoner");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(3);
    }

    @Test
    @DisplayName("Created tokens have the specified colors, creature types and abilities")
    void tokensHaveSpecifiedCharacteristics() {
        castSummoner();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertTokenCharacteristics("Knight", CardColor.WHITE, CardSubtype.KNIGHT, Keyword.VIGILANCE);
        assertTokenCharacteristics("Centaur", CardColor.GREEN, CardSubtype.CENTAUR);
        assertTokenCharacteristics("Rhino", CardColor.GREEN, CardSubtype.RHINO, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Esix replaces all three tokens in the single token creation event")
    void esixReplacesAllThreeTokensTogether() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player2, new TrostanisSummoner());
        castSummoner();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosenCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3)
                .allSatisfy(permanent -> assertThat(permanent.getCard().getName())
                        .isEqualTo("Trostani's Summoner"));
    }

    private void assertTokenCharacteristics(String name, CardColor color, CardSubtype subtype,
                                            Keyword... keywords) {
        assertThat(tokens(player1, name)).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColors()).containsExactly(color);
            assertThat(token.getCard().getSubtypes()).containsExactly(subtype);
            assertThat(token.getCard().getKeywords()).containsExactlyInAnyOrder(keywords);
            assertThat(token.isTapped()).isFalse();
        });
    }

    private void assertToken(Player player, String name, int power, int toughness, Keyword keyword) {
        List<Permanent> found = tokens(player, name);
        assertThat(found).hasSize(1);
        Permanent token = found.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(toughness);
        if (keyword != null) {
            assertThat(token.getCard().getKeywords()).contains(keyword);
        }
    }

    private List<Permanent> tokens(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .toList();
    }
}
