package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WickerwingEffigy.class, GrizzlyBears.class})
class WickerwingEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature from the top of the library and changes its characteristics")
    void changesCreatureCastFromLibrary() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, entered)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, entered)).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, entered, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(1);
    }

    @Test
    @DisplayName("does not change a creature cast from hand")
    void doesNotChangeCreatureCastFromHand() {
        harness.addToBattlefield(player1, new WickerwingEffigy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, entered)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, entered)).doesNotContain(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, entered, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, entered)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entered)).isEqualTo(2);
    }
}
