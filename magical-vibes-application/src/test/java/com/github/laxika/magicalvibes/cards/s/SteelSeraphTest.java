package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirMarshal;
import com.github.laxika.magicalvibes.cards.m.MachineOverMatter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelSeraph.class, AirMarshal.class, MachineOverMatter.class})
class SteelSeraphTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Prototype cast uses the alternate characteristics")
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new SteelSeraph()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent seraph = findPermanent(player1, "Steel Seraph");
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, seraph)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Beginning of combat grants the chosen keyword to a creature you control")
    void beginningOfCombatGrantsChosenKeyword() {
        harness.addToBattlefield(player1, new SteelSeraph());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirMarshal());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted keyword wears off at end of turn")
    void grantedKeywordWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SteelSeraph());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirMarshal());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FLYING");
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new SteelSeraph());
        harness.addToBattlefieldAndReturn(player1, new AirMarshal());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new SteelSeraph());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new AirMarshal());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Steel Seraph can grant vigilance to itself")
    void canGrantVigilanceToItself() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player1, new SteelSeraph());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, seraph.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "VIGILANCE");

        assertThat(gqs.hasKeyword(gd, seraph, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, seraph, Keyword.LIFELINK)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, seraph, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, seraph, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Prototype retains its combat ability and can grant itself lifelink")
    void prototypeRetainsCombatAbility() {
        harness.setHand(player1, List.of(new SteelSeraph()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        Permanent seraph = findPermanent(player1, "Steel Seraph");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, seraph.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(gqs.hasKeyword(gd, seraph, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, seraph, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, seraph)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seraph)).isEqualTo(3);
    }

    @Test
    @DisplayName("A bounced prototype can be cast normally with its full-sized characteristics")
    void bouncedPrototypeCanBeCastNormally() {
        harness.setHand(player1, List.of(new SteelSeraph()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();
        Permanent seraph = findPermanent(player1, "Steel Seraph");

        harness.setHand(player2, List.of(new MachineOverMatter()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, seraph.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Steel Seraph");
        harness.assertNotOnBattlefield(player1, "Steel Seraph");

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent recastSeraph = findPermanent(player1, "Steel Seraph");
        assertThat(gqs.getEffectivePower(gd, recastSeraph)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, recastSeraph)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, recastSeraph)).isEmpty();
    }
}
