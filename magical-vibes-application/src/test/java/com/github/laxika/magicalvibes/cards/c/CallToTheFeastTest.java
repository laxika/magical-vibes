package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallToTheFeast.class})
class CallToTheFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 white Vampire creature tokens with lifelink")
    void createsThreeVampireTokensWithLifelink() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CallToTheFeast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);

        List<Permanent> tokens = findPermanents(player1, "Vampire");
        assertThat(tokens).hasSize(3);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CallToTheFeast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Call to the Feast");
    }

    @Test
    @DisplayName("Vampire tokens gain life for their controller when dealing combat damage")
    void tokensGainLifeThroughCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CallToTheFeast()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        findPermanents(player1, "Vampire").forEach(token -> token.setSummoningSick(false));
        declareAttackers(List.of(0, 1, 2));
        resolveCombat();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }
}
