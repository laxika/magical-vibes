package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZoyowasJustice.class, Millstone.class, HillGiant.class, LlanowarElves.class, Plains.class})
class ZoyowasJusticeTest extends BaseCardTest {

    @Test
    void shufflesTargetArtifactAndItsOwnerDiscoversUsingManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player2, List.of(new Plains(), new HillGiant(), discovered));
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).hasSize(1);
        Card found = search.params().cards().getFirst();
        assertThat(found).isIn(discovered, target.getCard());
        harness.handleCardChosen(player2, -1);

        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gd.playerHands.get(player2.getId())).contains(found);
    }

    @Test
    void cannotTargetAZeroManaValueOrNonArtifactCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature with mana value 1 or greater");
    }
}
