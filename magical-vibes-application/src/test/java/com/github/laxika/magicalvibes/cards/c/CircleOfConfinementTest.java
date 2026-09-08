package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CircleOfConfinement.class, ChildOfNight.class, Naturalize.class, HillGiant.class,
        VampireNighthawk.class})
class CircleOfConfinementTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opposing creature with mana value 3 or less")
    void etbExilesSmallOpponentCreature() {
        harness.addToBattlefield(player2, new ChildOfNight());
        UUID creatureId = harness.getPermanentId(player2, "Child of Night");

        castCircle(creatureId);

        harness.assertNotOnBattlefield(player2, "Child of Night");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Child of Night"));
    }

    @Test
    @DisplayName("Exiled creature returns when Circle of Confinement leaves")
    void exiledCreatureReturnsWhenCircleLeaves() {
        harness.addToBattlefield(player2, new ChildOfNight());
        UUID creatureId = harness.getPermanentId(player2, "Child of Night");
        castCircle(creatureId);

        UUID circleId = harness.getPermanentId(player1, "Circle of Confinement");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, circleId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Child of Night");
    }

    @Test
    @DisplayName("Opponent casting the exiled Vampire's name gains 2 life")
    void matchingVampireSpellGainsLife() {
        harness.addToBattlefield(player2, new ChildOfNight());
        UUID creatureId = harness.getPermanentId(player2, "Child of Night");
        castCircle(creatureId);
        harness.setLife(player1, 10);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ChildOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("A differently named Vampire spell does not trigger")
    void differentlyNamedVampireSpellDoesNotGainLife() {
        harness.addToBattlefield(player2, new ChildOfNight());
        UUID creatureId = harness.getPermanentId(player2, "Child of Night");
        castCircle(creatureId);
        harness.setLife(player1, 10);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VampireNighthawk()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Cannot target an opposing creature with mana value greater than 3")
    void cannotTargetLargeCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID creatureId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new CircleOfConfinement()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCircle(UUID targetId) {
        harness.setHand(player1, List.of(new CircleOfConfinement()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
