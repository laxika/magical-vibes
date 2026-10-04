package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FrontierMastodon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenSurveyor.class, FrontierMastodon.class, Forest.class})
class AvenSurveyorTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Aven Surveyor")
    void counterModePutsCounterOnItself() {
        cast(0, null);

        Permanent surveyor = findPermanent(player1, "Aven Surveyor");
        assertThat(surveyor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, surveyor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, surveyor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bounce mode returns the target creature to its owner's hand")
    void bounceModeReturnsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrontierMastodon());

        cast(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Frontier Mastodon");
        harness.assertInHand(player2, "Frontier Mastodon");
    }

    @Test
    @DisplayName("Bounce mode rejects a noncreature target")
    void bounceModeRejectsNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new AvenSurveyor()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Return target creature to its owner's hand");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new AvenSurveyor()));
        addMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode == 0
                ? "Put a +1/+1 counter on this creature"
                : "Return target creature to its owner's hand");
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
        harness.passBothPriorities();
    }

    @Test
    void canBounceFriendlyCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrontierMastodon());
        cast(1, target.getId());

        harness.assertNotOnBattlefield(player1, "Frontier Mastodon");
        harness.assertInHand(player1, "Frontier Mastodon");
        assertThat(findPermanent(player1, "Aven Surveyor")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canBounceItselfWhenEnteringWithoutBeingCast() {
        Permanent surveyor = harness.enterBattlefieldAndReturn(player1, new AvenSurveyor());
        gs.passPriority(gd, player1);
        harness.handleListChoice(player1, "Return target creature to its owner's hand");
        harness.handlePermanentChosen(player1, surveyor.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aven Surveyor");
        harness.assertInHand(player1, "Aven Surveyor");
    }

    @Test
    void counterModeCanBeChosenWhenEnteringWithoutBeingCast() {
        Permanent surveyor = harness.enterBattlefieldAndReturn(player1, new AvenSurveyor());
        gs.passPriority(gd, player1);
        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        harness.passBothPriorities();

        assertThat(surveyor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
