package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeTreeBattalion.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class ThreeTreeBattalionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a qualifying creature onto the battlefield and creates a 1/1 duplicate")
    void putsCreatureAndCreatesDuplicate() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new HillGiant(), new Shock());

        castBattalion();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        List<Permanent> bearsOnBattlefield = findPermanents(player1, "Grizzly Bears");
        assertThat(bearsOnBattlefield).hasSize(2);
        assertThat(bearsOnBattlefield).anySatisfy(permanent -> {
            assertThat(permanent.getCard()).isSameAs(bears);
            assertThat(permanent.getEffectivePower()).isEqualTo(2);
            assertThat(permanent.getEffectiveToughness()).isEqualTo(2);
        });
        assertThat(bearsOnBattlefield).anySatisfy(permanent -> {
            assertThat(permanent.getCard()).isNotSameAs(bears);
            assertThat(permanent.getEffectivePower()).isEqualTo(1);
            assertThat(permanent.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Can decline to put a qualifying creature onto the battlefield")
    void mayDeclineCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new Shock(), new HillGiant());

        castBattalion();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBattalion() {
        harness.setHand(player1, List.of(new ThreeTreeBattalion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
