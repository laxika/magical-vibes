package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YavimayaSapherdTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Yavimaya Sapherd puts it on the battlefield")
    void castingPutsOnBattlefieldAndTriggersEtb() {
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Yavimaya Sapherd");
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("When Yavimaya Sapherd enters the battlefield, a Saproling token is created")
    void etbCreatesToken() {
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("ETB token is a 1/1 green Saproling creature token")
    void tokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent token = findPermanent(player1, "Saproling");

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getKeywords()).isEmpty();
    }
}
