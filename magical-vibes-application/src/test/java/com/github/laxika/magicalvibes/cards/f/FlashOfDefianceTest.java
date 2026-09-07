package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashOfDefiance.class, CoralMerfolk.class, GlorySeeker.class, GrizzlyBears.class})
class FlashOfDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Green and white creatures can't block this turn")
    void greenAndWhiteCreaturesCantBlock() {
        Permanent green = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent white = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent blue = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        castFromHand();

        assertThat(green.isCantBlockThisTurn()).isTrue();
        assertThat(white.isCantBlockThisTurn()).isTrue();
        assertThat(blue.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Flashback pays 3 life and exiles Flash of Defiance")
    void flashbackPaysLifeAndExiles() {
        Permanent green = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new FlashOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(green.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        harness.assertNotInGraveyard(player1, "Flash of Defiance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flash of Defiance"));
    }

    @Test
    @DisplayName("Flashback requires enough life")
    void flashbackRequiresEnoughLife() {
        gd.playerLifeTotals.put(player1.getId(), 2);
        harness.setGraveyard(player1, List.of(new FlashOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new FlashOfDefiance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, Map.of());
        harness.passBothPriorities();
    }
}
