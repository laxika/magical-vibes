package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YourOwnFaceMocksYou.class, GrizzlyBears.class})
class YourOwnFaceMocksYouTest extends BaseCardTest {

    @Test
    void createsTwoScarecrowsWhenNoCreaturesAreChosen() {
        resolveScheme(List.of());

        assertThat(findPermanents(player1, "Scarecrow")).hasSize(2).allSatisfy(scarecrow -> {
            assertThat(scarecrow.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, scarecrow)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, scarecrow)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    void createsOneCopyAndOneScarecrowForOneChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveScheme(List.of(target.getId()));

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(findPermanents(player1, "Scarecrow")).hasSize(1);
    }

    @Test
    void createsCopiesAndNoScarecrowsForTwoChosenCreatures() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveScheme(List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(findPermanents(player1, "Scarecrow")).isEmpty();
    }

    private void resolveScheme(List<java.util.UUID> targetIds) {
        YourOwnFaceMocksYou scheme = new YourOwnFaceMocksYou();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                null,
                targetIds));
        harness.passBothPriorities();
    }
}
