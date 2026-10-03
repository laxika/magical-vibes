package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dracogenesis.class, DragonWhelp.class, GrizzlyBears.class})
class DracogenesisTest extends BaseCardTest {

    @Test
    @DisplayName("The controller can cast Dragon spells without paying their mana costs")
    void controllerCastsDragonForFree() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new DragonWhelp()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting a Dragon for free does not spend mana")
    void freeDragonCastSpendsNoMana() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Dragon spells still require mana")
    void nonDragonSpellIsNotFree() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The opponent cannot use the controller's free Dragon casts")
    void opponentCannotCastDragonForFree() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player2, List.of(new DragonWhelp()));

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Dragon spells can be cast for free in the same turn")
    void freeCastingIsNotLimitedToOneDragonPerTurn() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new DragonWhelp(), new DragonWhelp()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon Whelp")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dracogenesis in the graveyard does not make Dragon spells free")
    void graveyardDracogenesisDoesNotGrantFreeCasting() {
        harness.setGraveyard(player1, List.of(new Dracogenesis()));
        harness.setHand(player1, List.of(new DragonWhelp()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Free Dragon spells must still obey normal timing restrictions")
    void freeCastingDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.setHand(player1, List.of(new DragonWhelp()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("Dracogenesis does not waive Dragon activated ability costs")
    void dragonActivatedAbilityStillRequiresMana() {
        harness.addToBattlefield(player1, new Dracogenesis());
        harness.addToBattlefield(player1, new DragonWhelp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
