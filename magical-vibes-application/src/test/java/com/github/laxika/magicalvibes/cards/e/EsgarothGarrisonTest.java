package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsgarothGarrison.class, Forest.class, GrizzlyBears.class})
class EsgarothGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 5")
    void powerEqualsControlledCreatures() {
        Permanent garrison = addCreatureReady(player1, new EsgarothGarrison());

        assertThat(gqs.getEffectivePower(gd, garrison)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, garrison)).isEqualTo(5);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, garrison)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, garrison)).isEqualTo(5);
    }

    @Test
    @DisplayName("Recruit creates a Soldier after discarding a nonland card")
    void recruitCreatesSoldierForNonlandDiscard() {
        castAndResolve(new GrizzlyBears(), new Forest());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    @DisplayName("Recruit does not create a Soldier after discarding a land card")
    void recruitDoesNotCreateSoldierForLandDiscard() {
        castAndResolve(new Forest(), new GrizzlyBears());

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    private void castAndResolve(Card discardedCard, Card drawnCard) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new EsgarothGarrison(), discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }
    }
}
