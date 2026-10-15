package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzledOutrider;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

@CardUsed({DoomskarTitan.class, GrizzledOutrider.class})
class DoomskarTitanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives your creatures +1/+0 and haste until end of turn")
    void etbBoostsAndHastesYourCreatures() {
        harness.addToBattlefield(player1, new GrizzledOutrider());
        harness.setHand(player1, List.of(new DoomskarTitan()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzled Outrider");
        Permanent titan = findPermanent(player1, "Doomskar Titan");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(5);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(titan.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ETB boost and haste wear off at end of turn")
    void etbEffectWearsOffAtEndOfTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.addToBattlefield(player1, new GrizzledOutrider());
        harness.setHand(player1, List.of(new DoomskarTitan()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        Permanent bears = findPermanent(player1, "Grizzled Outrider");
        Permanent titan = findPermanent(player1, "Doomskar Titan");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(titan.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the ETB resolves are not affected")
    void laterCreaturesAreNotAffected() {
        harness.setHand(player1, List.of(new DoomskarTitan()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzledOutrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzled Outrider");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        DoomskarTitan titan = new DoomskarTitan();
        harness.setHand(player1, List.of(titan));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(titan.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, titan.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doomskar Titan");
    }

    @Test
    @DisplayName("ETB does not boost or grant haste to opposing creatures")
    void opposingCreaturesAreNotAffected() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzledOutrider());
        harness.setHand(player1, List.of(new DoomskarTitan()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(5);
        assertThat(opponent.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("ETB affects creatures present when the trigger resolves")
    void creaturesEnteringBeforeTriggerResolvesAreAffected() {
        harness.setHand(player1, List.of(new DoomskarTitan()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new GrizzledOutrider());
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);
        assertThat(outrider.hasKeyword(Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
        assertThat(outrider.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot cast a foretold Titan during the turn it was foretold")
    void cannotCastOnTheTurnItWasForetold() {
        DoomskarTitan titan = new DoomskarTitan();
        harness.setHand(player1, List.of(titan));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromExile(player1, titan.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(titan.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Doomskar Titan");
    }
}
