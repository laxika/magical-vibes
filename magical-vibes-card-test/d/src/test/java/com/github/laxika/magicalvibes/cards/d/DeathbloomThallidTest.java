package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathbloomThallid.class, WrathOfGod.class})
class DeathbloomThallidTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Deathbloom Thallid puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new DeathbloomThallid()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Deathbloom Thallid");
    }

    @Test
    @DisplayName("When Deathbloom Thallid dies, a Saproling token is created")
    void deathTriggerCreatesToken() {
        harness.addToBattlefield(player1, new DeathbloomThallid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();

        // Deathbloom Thallid should be in the graveyard
        harness.assertInGraveyard(player1, "Deathbloom Thallid");

        // One death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger
        harness.passBothPriorities();

        // A Saproling token should be on the battlefield
        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("Death trigger token is a 1/1 green Saproling creature")
    void tokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new DeathbloomThallid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Resolve death trigger

        Permanent token = findPermanent(player1, "Saproling");

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("An opponent's dying Thallid creates a token for that opponent")
    void opponentsDeathTriggerCreatesTokenForOpponent() {
        harness.addToBattlefield(player2, new DeathbloomThallid());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player2, "Deathbloom Thallid");
        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
