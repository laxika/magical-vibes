package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Lictor.class, GrizzlyBears.class, Forest.class})
class LictorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a trampling Tyranid Warrior when an opponent's creature entered this turn")
    void createsTokenAfterOpponentCreatureEnters() {
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveLictor();

        Permanent token = findPermanent(player1, "Tyranid Warrior");
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.TYRANID, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not create a token when only your creature entered this turn")
    void doesNotCreateTokenAfterOwnCreatureEnters() {
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolveLictor();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isZero();
    }

    @Test
    @DisplayName("Does not create a token when only an opponent's noncreature permanent entered")
    void doesNotCreateTokenAfterOpponentNoncreatureEnters() {
        harness.enterBattlefieldAndReturn(player2, new Forest());

        castAndResolveLictor();

        assertThat(countPermanents(player1, "Tyranid Warrior")).isZero();
    }

    private void castAndResolveLictor() {
        harness.setHand(player1, List.of(new Lictor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
