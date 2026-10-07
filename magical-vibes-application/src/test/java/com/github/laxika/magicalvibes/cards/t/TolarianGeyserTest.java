package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MoltenMonstrosity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolarianGeyser.class, MoltenMonstrosity.class, Island.class})
class TolarianGeyserTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureAndDrawsWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        harness.setLibrary(player1, List.of(new MoltenMonstrosity()));
        cast(false, target);

        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
        harness.assertInHand(player2, "Molten Monstrosity");
        harness.assertInHand(player1, "Molten Monstrosity");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void kickedSpellReturnsTargetCreatureDrawsAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        harness.setLibrary(player1, List.of(new MoltenMonstrosity()));
        cast(true, target);

        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
        harness.assertInHand(player2, "Molten Monstrosity");
        harness.assertInHand(player1, "Molten Monstrosity");
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new TolarianGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canReturnYourOwnCreatureAndDrawExactlyOneCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoltenMonstrosity());
        Island drawnCard = new Island();
        Island remainingCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));

        cast(false, target);

        harness.assertNotOnBattlefield(player1, "Molten Monstrosity");
        harness.assertInHand(player1, "Molten Monstrosity");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target.getCard(), drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Tolarian Geyser");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void illegalTargetPreventsDrawingAndGainingLife(boolean kicked) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        Island undrawnCard = new Island();
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.setHand(player1, List.of(new TolarianGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        if (kicked) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, target.getId(), null);
        } else {
            harness.castSorcery(player1, 0, target.getId());
        }
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Tolarian Geyser");
        harness.assertNotInHand(player2, "Molten Monstrosity");
    }

    @Test
    void returnsCreatureToItsOwnerRatherThanItsController() {
        MoltenMonstrosity creature = new MoltenMonstrosity();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));

        cast(true, target);

        harness.assertNotOnBattlefield(player1, "Molten Monstrosity");
        harness.assertInHand(player2, "Molten Monstrosity");
        harness.assertNotInHand(player1, "Molten Monstrosity");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotKickWithoutWhiteMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        harness.setHand(player1, List.of(new TolarianGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(boolean kicked, Permanent target) {
        harness.setHand(player1, List.of(new TolarianGeyser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        if (kicked) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, target.getId(), null);
            harness.passBothPriorities();
        } else {
            harness.castAndResolveSorcery(player1, 0, target.getId());
        }
    }
}
