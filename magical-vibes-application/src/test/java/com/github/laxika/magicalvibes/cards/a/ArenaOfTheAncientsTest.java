package com.github.laxika.magicalvibes.cards.a;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.j.Johan;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.z.ZephyrFalcon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ArenaOfTheAncients.class, Johan.class, ZephyrFalcon.class, Karakas.class})
class ArenaOfTheAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield taps all legendary creatures")
    void entersAndTapsLegendaryCreatures() {
        Permanent ownLegendary = harness.addToBattlefieldAndReturn(player1, new Johan());
        Permanent opponentLegendary = harness.addToBattlefieldAndReturn(player2, new Johan());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player2, new ZephyrFalcon());
        Permanent legendaryLand = harness.addToBattlefieldAndReturn(player2, new Karakas());

        harness.setHand(player1, List.of(new ArenaOfTheAncients()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownLegendary.isTapped()).isTrue();
        assertThat(opponentLegendary.isTapped()).isTrue();
        assertThat(nonlegendary.isTapped()).isFalse();
        assertThat(legendaryLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Legendary creatures remain tapped during their controllers' untap steps")
    void legendaryCreaturesDoNotUntap() {
        harness.addToBattlefield(player1, new ArenaOfTheAncients());
        Permanent opponentLegendary = harness.addToBattlefieldAndReturn(player2, new Johan());
        Permanent opponentNonlegendary = harness.addToBattlefieldAndReturn(player2, new ZephyrFalcon());
        opponentLegendary.tap();
        opponentNonlegendary.tap();

        advanceToUpkeep(player2);

        assertThat(opponentLegendary.isTapped()).isTrue();
        assertThat(opponentNonlegendary.isTapped()).isFalse();
    }
}
