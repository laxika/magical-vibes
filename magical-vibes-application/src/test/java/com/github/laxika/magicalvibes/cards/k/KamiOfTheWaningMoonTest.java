package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfTheWaningMoon.class, DampenThought.class, KondasHatamoto.class})
class KamiOfTheWaningMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell gives target creature fear")
    void spiritCastGrantsFear() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KondasHatamoto());

        harness.castFromHand(player1, new KamiOfTheWaningMoon(), "{2}{B}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Casting an Arcane spell gives target creature fear")
    void arcaneCastGrantsFear() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KondasHatamoto());
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOff() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KondasHatamoto());
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        harness.castFromHand(player1, new KondasHatamoto(), "{1}{W}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The triggered ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KondasHatamoto());

        harness.castFromHand(player1, new KamiOfTheWaningMoon(), "{2}{B}");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Arcane spell does not trigger Kami of the Waning Moon")
    void opponentArcaneSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KamiOfTheWaningMoon());
        harness.setHand(player2, List.of(new DampenThought()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }
}
