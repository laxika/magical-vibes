package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TripNoose;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Abolish.class, Crusade.class, Island.class, Plains.class, TripNoose.class})
class AbolishTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by discarding a Plains to destroy an artifact")
    void destroysArtifactWithAlternateCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TripNoose());
        harness.setHand(player1, List.of(new Abolish(), new Plains()));

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abolish");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Trip Noose");
    }

    @Test
    @DisplayName("Can destroy an enchantment")
    void destroysEnchantmentWithAlternateCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Crusade());
        harness.setHand(player1, List.of(new Abolish(), new Plains()));

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crusade");
    }

    @Test
    @DisplayName("Can be cast for its mana cost to destroy an artifact")
    void destroysArtifactWithNormalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TripNoose());
        harness.setHand(player1, List.of(new Abolish()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Abolish");
        harness.assertInGraveyard(player2, "Trip Noose");
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor an enchantment")
    void rejectsInvalidTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Abolish(), new Plains()));

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Abolish");
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Alternate cost requires discarding a Plains")
    void alternateCostRequiresPlains() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TripNoose());
        harness.setHand(player1, List.of(new Abolish(), new Island()));

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
