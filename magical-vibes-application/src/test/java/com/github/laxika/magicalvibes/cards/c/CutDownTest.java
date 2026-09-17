package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutDown.class, GoblinPiker.class, AirElemental.class, FountainOfYouth.class})
class CutDownTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with total power and toughness 5 or less")
    void destroysSmallCreature() {
        Permanent target = addCreatureReady(player2, new GoblinPiker());
        target.setPowerModifier(2);

        castCutDown(target);

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertInGraveyard(player2, "Goblin Piker");
    }

    @Test
    @DisplayName("Rejects a creature whose total power and toughness exceeds 5")
    void rejectsLargeCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power and toughness 5 or less");
    }

    @Test
    @DisplayName("Rechecks the power and toughness restriction on resolution")
    void fizzlesWhenTargetBecomesTooLarge() {
        Permanent target = addCreatureReady(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        target.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Rejects a noncreature permanent")
    void rejectsNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castCutDown(Permanent target) {
        harness.setHand(player1, List.of(new CutDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
