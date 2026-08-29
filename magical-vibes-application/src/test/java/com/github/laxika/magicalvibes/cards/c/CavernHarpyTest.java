package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.FoulFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cavern Harpy")
class CavernHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return a blue or black creature you control")
    void etbPromptsForBlueOrBlackCreature() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new CloudSprite()).getId();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new FoulFamiliar()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new CloudSprite());
        castAndResolveSpell();

        UUID harpyId = harness.getPermanentId(player1, "Cavern Harpy");
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(blueId, blackId, harpyId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing a matching creature returns it to its owner's hand")
    void chosenMatchingCreatureReturnsToHand() {
        UUID familiarId = harness.addToBattlefieldAndReturn(player1, new FoulFamiliar()).getId();
        castAndResolveSpell();

        harness.handlePermanentChosen(player1, familiarId);

        harness.assertInHand(player1, "Foul Familiar");
        harness.assertOnBattlefield(player1, "Cavern Harpy");
    }

    @Test
    @DisplayName("Paying 1 life returns Cavern Harpy to its owner's hand")
    void payLifeReturnsSelfToHand() {
        harness.addToBattlefield(player1, new CavernHarpy());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertInHand(player1, "Cavern Harpy");
        harness.assertNotOnBattlefield(player1, "Cavern Harpy");
    }

    @Test
    @DisplayName("Cannot pay 1 life from 0 life")
    void cannotPayLifeFromZero() {
        harness.addToBattlefield(player1, new CavernHarpy());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    private void castAndResolveSpell() {
        harness.setHand(player1, List.of(new CavernHarpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
