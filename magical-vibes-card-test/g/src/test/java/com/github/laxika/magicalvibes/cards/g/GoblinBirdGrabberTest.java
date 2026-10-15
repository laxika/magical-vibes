package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpectralSailor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBirdGrabber.class, SpectralSailor.class, Shock.class})
class GoblinBirdGrabberTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying when its controller controls a creature with flying")
    void gainsFlyingWithAnotherFlyingCreature() {
        Permanent grabber = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        harness.addToBattlefield(player1, new SpectralSailor());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without controlling a creature with flying")
    void cannotActivateWithoutFlyingCreature() {
        harness.addToBattlefield(player1, new GoblinBirdGrabber());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control a creature with flying");
    }

    @Test
    @DisplayName("Flying wears off at the end of the turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent grabber = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        harness.addToBattlefield(player1, new SpectralSailor());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's flying creature does not enable activation")
    void cannotActivateWithOnlyOpponentsFlyingCreature() {
        harness.addToBattlefield(player1, new GoblinBirdGrabber());
        harness.addToBattlefield(player2, new SpectralSailor());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control a creature with flying");
    }

    @Test
    @DisplayName("The ability resolves even if the enabling flyer dies in response")
    void resolvesAfterEnablingFlyerDies() {
        Permanent grabber = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        Permanent sailor = harness.addToBattlefieldAndReturn(player1, new SpectralSailor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, sailor.getId());
        harness.assertInGraveyard(player1, "Spectral Sailor");
        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Once flying, Goblin Bird-Grabber itself satisfies its activation restriction")
    void canActivateUsingItsOwnGrantedFlying() {
        Permanent grabber = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        Permanent sailor = harness.addToBattlefieldAndReturn(player1, new SpectralSailor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, sailor.getId());
        harness.assertInGraveyard(player1, "Spectral Sailor");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
