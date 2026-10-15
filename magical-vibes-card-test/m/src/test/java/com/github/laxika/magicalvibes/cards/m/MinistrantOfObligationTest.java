package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinistrantOfObligation.class, WrathOfGod.class})
class MinistrantOfObligationTest extends BaseCardTest {

    @Test
    @DisplayName("Afterlife 2 creates two 1/1 white and black Spirit tokens with flying")
    void afterlifeCreatesTwoSpiritTokens() {
        harness.addToBattlefield(player1, new MinistrantOfObligation());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player1, "Ministrant of Obligation");

        List<Permanent> tokens = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Afterlife waits for its triggered ability to resolve")
    void afterlifeUsesTheStack() {
        harness.addToBattlefield(player1, new MinistrantOfObligation());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Ministrant of Obligation");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCard().getName()).isEqualTo("Spirit"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths give each controller their own two Spirits")
    void simultaneousDeathsCreateTokensForEachController() {
        harness.addToBattlefield(player1, new MinistrantOfObligation());
        harness.addToBattlefield(player2, new MinistrantOfObligation());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ministrant of Obligation");
        harness.assertInGraveyard(player2, "Ministrant of Obligation");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCard().getName()).isEqualTo("Spirit"));
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCard().getName()).isEqualTo("Spirit"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Spirit tokens do not inherit afterlife")
    void dyingSpiritTokensDoNotCreateMoreTokens() {
        harness.addToBattlefield(player1, new MinistrantOfObligation());
        harness.setHand(player1, List.of(new WrathOfGod(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
