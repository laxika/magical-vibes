package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Crypsis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OreskosSunGuide.class, Crypsis.class})
class OreskosSunGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Oreskos Sun Guide makes its controller gain 2 life")
    void untappingGainsLife() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        guide.tap();
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    void alreadyUntappedGuideDoesNotGainLifeDuringUntapStep() {
        harness.addToBattlefield(player1, new OreskosSunGuide());
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
    }

    @Test
    void onlyUntappingGuidesControllerGainsLife() {
        Permanent guide = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        guide.tap();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(guide.isTapped()).isTrue();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 12);
    }

    @Test
    void spellUntappingGuideTriggersEveryTimeItBecomesUntapped() {
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        harness.setLife(player1, 10);

        guide.tap();
        castCrypsis(guide);
        harness.assertLife(player1, 12);

        castCrypsis(guide);
        harness.assertLife(player1, 12);

        guide.tap();
        castCrypsis(guide);
        harness.assertLife(player1, 14);
    }

    private void castCrypsis(Permanent guide) {
        harness.setHand(player1, List.of(new Crypsis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, guide.getId());
        resolveAllTriggers();
        assertThat(guide.isTapped()).isFalse();
    }
}
