package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoxingRing.class, GiantTortoise.class, GrizzlyBears.class, HillGiant.class})
class BoxingRingTest extends BaseCardTest {

    @Test
    @DisplayName("An entering creature may fight only an opposing creature with the same mana value")
    void fightsSameManaValueCreature() {
        harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        Permanent sameManaValue = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentManaValue = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, sameManaValue.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.findPermanentById(gd, differentManaValue.getId())).isNotNull();
    }

    @Test
    @DisplayName("Treasure ability requires a creature that fought this turn")
    void createsTreasureAfterCreatureFights() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());
        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, tortoise.getId());
        harness.passBothPriorities();

        Permanent grizzly = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gd.permanentsThatFoughtThisTurn).contains(grizzly.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ring), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger rejects an opposing creature with a different mana value")
    void rejectsDifferentManaValueTarget() {
        harness.addToBattlefieldAndReturn(player1, new BoxingRing());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentManaValue = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, differentManaValue.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Player player,
                              com.github.laxika.magicalvibes.model.Card creature) {
        harness.setHand(player, List.of(creature));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
