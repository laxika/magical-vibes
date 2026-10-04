package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeatherbrainedFilcher.class, Shock.class})
class FeatherbrainedFilcherTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it leaves the battlefield")
    void createsFoodWhenLeavingBattlefield() {
        harness.addToBattlefield(player1, new FeatherbrainedFilcher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Featherbrained Filcher"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Featherbrained Filcher");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("The created Food token can be sacrificed for life")
    void foodTokenCanBeSacrificed() {
        harness.addToBattlefield(player1, new FeatherbrainedFilcher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Featherbrained Filcher"));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Returning to hand creates exactly one Food after the trigger resolves")
    void createsFoodWhenReturnedToHand() {
        harness.addToBattlefield(player1, new FeatherbrainedFilcher());
        var filcher = findPermanent(player1, "Featherbrained Filcher");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, filcher));

        harness.assertInHand(player1, "Featherbrained Filcher");
        assertThat(countPermanents(player1, "Food")).isZero();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Exile creates Food for the creature's controller")
    void createsFoodWhenExiled() {
        harness.addToBattlefield(player2, new FeatherbrainedFilcher());
        var filcher = findPermanent(player2, "Featherbrained Filcher");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, filcher));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Featherbrained Filcher");
        harness.assertNotInGraveyard(player2, "Featherbrained Filcher");
        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Returning to the library also creates Food")
    void createsFoodWhenReturnedToLibrary() {
        harness.addToBattlefield(player1, new FeatherbrainedFilcher());
        var filcher = findPermanent(player1, "Featherbrained Filcher");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToLibraryTop(gd, filcher));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(filcher.getCard());
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Food requires two mana and an untapped source, and sacrifice is paid before life gain")
    void foodActivationPaysCostsBeforeResolving() {
        harness.addToBattlefield(player1, new FeatherbrainedFilcher());
        var filcher = findPermanent(player1, "Featherbrained Filcher");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, filcher));
        resolveAllTriggers();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        var food = findPermanent(player1, "Food");
        food.tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Food");

        food.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }
}
