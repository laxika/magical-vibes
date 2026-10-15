package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.Afflict;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimbleMongoose.class, Afflict.class, KamahlPitFighter.class})
class NimbleMongooseTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 1/1 with fewer than seven cards in its controller's graveyard")
    void remainsBaseSizeBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 at seven cards in its controller's graveyard")
    void getsBoostAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only its controller's graveyard")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shroud prevents spells from targeting it")
    void shroudPreventsSpellsFromTargeting() {
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mongoose.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shroud prevents abilities from targeting it")
    void shroudPreventsAbilitiesFromTargeting() {
        addCreatureReady(player1, new KamahlPitFighter());
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mongoose.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Loses the boost when its controller's graveyard drops below seven cards")
    void losesBoostBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(3);

        harness.setGraveyard(player1, graveyardCards(6));

        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains threshold immediately when the graveyard reaches seven cards")
    void gainsBoostWhileOnBattlefield() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent mongoose = harness.addToBattlefieldAndReturn(player1, new NimbleMongoose());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new KamahlPitFighter());

        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(1);

        harness.setGraveyard(player1, graveyardCards(7));

        assertThat(gqs.getEffectivePower(gd, mongoose)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mongoose)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud also prevents an opponent's spell from targeting it at threshold")
    void shroudPreventsOpponentSpellAtThreshold() {
        harness.setGraveyard(player2, graveyardCards(7));
        Permanent mongoose = harness.addToBattlefieldAndReturn(player2, new NimbleMongoose());
        harness.setHand(player1, List.of(new Afflict()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mongoose.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud also prevents an opponent's activated ability from targeting it")
    void shroudPreventsOpponentAbility() {
        addCreatureReady(player1, new KamahlPitFighter());
        Permanent mongoose = harness.addToBattlefieldAndReturn(player2, new NimbleMongoose());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mongoose.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Afflict());
        }
        return cards;
    }
}
