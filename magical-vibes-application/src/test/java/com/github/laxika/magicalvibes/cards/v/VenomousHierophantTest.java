package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomousHierophant.class, Forest.class})
class VenomousHierophantTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards")
    void entersAndMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new VenomousHierophant());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(Forest.class, Forest.class, Forest.class);
    }
}
