package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackbloomRogue.class, BlackbloomBog.class, Spellbook.class})
class BlackbloomRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 when an opponent has eight cards in their graveyard")
    void thresholdBoost() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new BlackbloomRogue());
        harness.setGraveyard(player2, graveyardOfSize(7));

        assertStats(rogue, 2, 3);

        harness.setGraveyard(player2, graveyardOfSize(8));

        assertStats(rogue, 5, 3);
    }

    @Test
    @DisplayName("The controller's graveyard does not enable the threshold boost")
    void ownGraveyardDoesNotCount() {
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new BlackbloomRogue());
        harness.setGraveyard(player1, graveyardOfSize(8));

        assertStats(rogue, 2, 3);
    }

    @Test
    @DisplayName("Blackbloom Bog enters tapped and produces black mana")
    void landFaceEntersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new BlackbloomRogue()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(BlackbloomBog.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }

    private void assertStats(Permanent rogue, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, rogue)).isEqualTo(toughness);
    }
}
