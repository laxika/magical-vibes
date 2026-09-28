package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoThePit.class, GrizzlyBears.class, Opt.class})
class IntoThePitTest extends BaseCardTest {

    @Test
    void castsTopSpellBySacrificingANonlandPermanent() {
        harness.addToBattlefield(player1, new IntoThePit());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTopWithAdditionalCost(player1, fodder.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(opt, fodder.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(opt);
    }

    @Test
    void maySacrificeIntoThePitItselfToCastFromTheTop() {
        Permanent intoThePit = harness.addToBattlefieldAndReturn(player1, new IntoThePit());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTopWithAdditionalCost(player1, intoThePit.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(opt, intoThePit.getCard());
    }

    @Test
    void requiresTheSacrificeSelectionBeforeRemovingTheTopCard() {
        harness.addToBattlefield(player1, new IntoThePit());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opt);
    }
}
