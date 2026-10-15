package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YavimayaSapherd.class})
class YavimayaSapherdTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Yavimaya Sapherd puts it on the battlefield")
    void castingPutsOnBattlefieldAndTriggersEtb() {
        harness.castFromHand(player1, new YavimayaSapherd(), "{2}{G}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Yavimaya Sapherd");
    }

    @Test
    @DisplayName("When Yavimaya Sapherd enters the battlefield, a Saproling token is created")
    void etbCreatesToken() {
        harness.castFromHand(player1, new YavimayaSapherd(), "{2}{G}");
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("ETB token is a 1/1 green Saproling creature token")
    void tokenHasCorrectProperties() {
        harness.castFromHand(player1, new YavimayaSapherd(), "{2}{G}");
        resolveAllTriggers();

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
    void tokenIsCreatedOnlyWhenEntryTriggerResolves() {
        harness.castFromHand(player1, new YavimayaSapherd(), "{2}{G}");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Yavimaya Sapherd");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    void enteringWithoutCastingCreatesTokenForEnteringCreaturesController() {
        harness.enterBattlefieldAndReturn(player2, new YavimayaSapherd());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    void entryTriggerStillCreatesTokenAfterSourceLeavesBattlefield() {
        Permanent sapherd = harness.enterBattlefieldAndReturn(player1, new YavimayaSapherd());
        gd.playerBattlefields.get(player1.getId()).remove(sapherd);
        gd.playerGraveyards.get(player1.getId()).add(sapherd.getCard());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Yavimaya Sapherd")).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }
}
