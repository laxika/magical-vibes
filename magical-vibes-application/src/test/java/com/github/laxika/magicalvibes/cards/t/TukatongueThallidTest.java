package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TukatongueThallid.class, WrathOfGod.class})
class TukatongueThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Tukatongue Thallid puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new TukatongueThallid()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tukatongue Thallid");
    }

    @Test
    @DisplayName("When Tukatongue Thallid dies, a Saproling token is created")
    void deathTriggerCreatesToken() {
        harness.addToBattlefield(player1, new TukatongueThallid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities(); // Resolve Wrath: Tukatongue Thallid dies

        GameData gd = harness.getGameData();

        // Tukatongue Thallid should be in the graveyard
        harness.assertInGraveyard(player1, "Tukatongue Thallid");

        // One death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger
        harness.passBothPriorities();

        // A Saproling token should be on the battlefield
        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("A dying opponent's Thallid creates its token for that opponent")
    void opponentDeathCreatesTokenForItsController() {
        harness.addToBattlefield(player2, new TukatongueThallid());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Tukatongue Thallid");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        Permanent token = findPermanent(player2, "Saproling");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous Thallid deaths each create a token after the board wipe")
    void simultaneousDeathsCreateSeparateTokens() {
        harness.addToBattlefield(player1, new TukatongueThallid());
        harness.addToBattlefield(player2, new TukatongueThallid());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tukatongue Thallid");
        harness.assertInGraveyard(player2, "Tukatongue Thallid");
        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
