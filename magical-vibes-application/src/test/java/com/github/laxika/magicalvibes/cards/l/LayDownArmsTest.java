package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AmbushParatrooper;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LayDownArms.class, AmbushParatrooper.class, Plains.class})
class LayDownArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature within the Plains count and its controller gains 3 life")
    void exilesCreatureAndGivesLifeToItsController() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent target = addCreatureReady(player2, new AmbushParatrooper());
        int lifeBefore = gd.getLife(player2.getId());

        castLayDownArms(target);

        harness.assertNotOnBattlefield(player2, "Ambush Paratrooper");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Ambush Paratrooper"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value exceeds the Plains count")
    void cannotTargetCreatureAbovePlainsCount() {
        harness.addToBattlefield(player1, new Plains());
        Permanent target = addCreatureReady(player2, new AmbushParatrooper());
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts only Plains controlled by the spell's controller")
    void countsPlainsControlledByCaster() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        Permanent target = addCreatureReady(player2, new AmbushParatrooper());
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile your own creature and gives you the life")
    void canExileOwnCreature() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AmbushParatrooper());
        int casterLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        castLayDownArms(target);

        harness.assertNotOnBattlefield(player1, "Ambush Paratrooper");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ambush Paratrooper"));
        harness.assertLife(player1, casterLife + 3);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    @DisplayName("Losing a Plains before resolution makes the target illegal and prevents life gain")
    void rechecksPlainsCountOnResolution() {
        harness.addToBattlefield(player1, new Plains());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AmbushParatrooper());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, plains));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ambush Paratrooper");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player2, lifeBefore);
        harness.assertInGraveyard(player1, "Lay Down Arms");
    }

    @Test
    @DisplayName("A target that leaves the battlefield prevents life gain")
    void missingTargetPreventsLifeGain() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AmbushParatrooper());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ambush Paratrooper");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature even when its mana value is within the Plains count")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLayDownArms(Permanent target) {
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
