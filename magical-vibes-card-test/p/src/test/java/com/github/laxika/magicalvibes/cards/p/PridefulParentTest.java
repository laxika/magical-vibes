package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PridefulParent.class})
class PridefulParentTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Cat token")
    void entersCreatesCatToken() {
        harness.setHand(player1, List.of(new PridefulParent()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Cat");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CAT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Cat is created only when the entry trigger resolves")
    void tokenCreationWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new PridefulParent()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(findPermanent(player1, "Cat").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast creates a Cat for the entering creature's controller")
    void noncastEntryCreatesTokenForOpponent() {
        harness.enterBattlefieldAndReturn(player2, new PridefulParent());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(findPermanent(player2, "Cat").getCard().isToken()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking does not tap Prideful Parent")
    void vigilanceKeepsParentUntapped() {
        Permanent parent = addCreatureReady(player1, new PridefulParent());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(parent.isAttacking()).isTrue();
        assertThat(parent.isTapped()).isFalse();
    }
}
