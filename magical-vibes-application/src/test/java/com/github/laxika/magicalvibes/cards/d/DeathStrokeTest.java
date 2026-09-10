package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VolrathsGardens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathStroke.class, DauthiTrapper.class, VolrathsGardens.class})
class DeathStrokeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target tapped creature")
    void destroysTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DauthiTrapper());
        creature.tap();

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Dauthi Trapper");
        harness.assertInGraveyard(player2, "Dauthi Trapper");
    }

    @Test
    @DisplayName("Destroys a tapped creature you control")
    void destroysOwnTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DauthiTrapper());
        creature.tap();

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Dauthi Trapper");
        harness.assertInGraveyard(player1, "Dauthi Trapper");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DauthiTrapper());

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreaturePermanent() {
        Permanent gardens = harness.addToBattlefieldAndReturn(player2, new VolrathsGardens());
        gardens.tap();

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(gardens.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy a creature that becomes untapped before resolution")
    void doesNotDestroyCreatureThatBecomesUntappedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DauthiTrapper());
        creature.tap();

        harness.setHand(player1, List.of(new DeathStroke()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        creature.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dauthi Trapper");
        harness.assertNotInGraveyard(player2, "Dauthi Trapper");
    }
}
