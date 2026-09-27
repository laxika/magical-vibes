package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpaceMarineDevastator.class, GildedLotus.class, GrizzlyBears.class})
class SpaceMarineDevastatorTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Space Marine Devastator")).hasSize(3);
        assertThat(findPermanents(player1, "Space Marine Devastator"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Grav-cannon destroys a target artifact when it enters")
    void gravCannonDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gilded Lotus");
        harness.assertOnBattlefield(player1, "Space Marine Devastator");
    }

    @Test
    @DisplayName("Grav-cannon cannot target a creature")
    void gravCannonRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }
}
