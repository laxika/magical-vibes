package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.cards.r.ResoundingWave;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SproutingThrinax.class, WrathOfGod.class, ResoundingThunder.class, ResoundingWave.class})
class SproutingThrinaxTest extends BaseCardTest {

    @Test
    @DisplayName("When Sprouting Thrinax dies, three 1/1 green Saproling tokens are created")
    void deathTriggerCreatesThreeSaprolings() {
        harness.addToBattlefield(player1, new SproutingThrinax());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Sprouting Thrinax");

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.passBothPriorities(); // Resolve death trigger

        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(3);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
            assertThat(token.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("Lethal damage creates Saprolings for the last controller, not the owner")
    void lethalDamageCreatesTokensForLastController() {
        SproutingThrinax thrinax = new SproutingThrinax();
        thrinax.setOwnerId(player1.getId());
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, thrinax);
        harness.setHand(player1, List.of(new ResoundingThunder()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.assertInGraveyard(player1, "Sprouting Thrinax");
        harness.assertNotOnBattlefield(player2, "Sprouting Thrinax");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player2, "Saproling")).hasSize(3);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Each Thrinax dying simultaneously creates its own three Saprolings")
    void simultaneousDeathsEachCreateThreeTokens() {
        harness.addToBattlefield(player1, new SproutingThrinax());
        harness.addToBattlefield(player1, new SproutingThrinax());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).hasSize(6);
        assertThat(findPermanents(player1, "Sprouting Thrinax")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Returning Thrinax to hand does not trigger token creation")
    void returningToHandDoesNotCreateTokens() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new SproutingThrinax());
        harness.setHand(player1, List.of(new ResoundingWave()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, permanent.getId());

        harness.assertInHand(player1, "Sprouting Thrinax");
        harness.assertNotOnBattlefield(player1, "Sprouting Thrinax");
        harness.assertNotInGraveyard(player1, "Sprouting Thrinax");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }
}
