package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
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

@CardUsed({KarolinaDeanRunaway.class, ThinkTwice.class, SwiftfootBoots.class})
class KarolinaDeanRunawayTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one mana of each color to the controller's restricted pool at their first main phase")
    void addsOneManaOfEachColorAtFirstMainPhase() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isZero();

        resolveAllTriggers();

        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(color)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Does not trigger during an opponent's first main phase")
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getNonHandSpellOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("The mana cannot pay for a spell cast from hand")
    void manaCannotPayForSpellFromHand() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new ThinkTwice()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana can pay for a spell cast from the graveyard")
    void manaCanPayForSpellFromGraveyard() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new KarolinaDeanRunaway()));

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Karolina Dean, Runaway");
        harness.assertNotInGraveyard(player1, "Think Twice");
    }

    @Test
    @DisplayName("The restricted mana alone can pay the full flashback cost")
    void restrictedManaPaysFullFlashbackCost() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        ThinkTwice spell = new ThinkTwice();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new KarolinaDeanRunaway()));

        harness.castFromGraveyard(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isEqualTo(2);
        resolveAllTriggers();

        harness.assertInHand(player1, "Karolina Dean, Runaway");
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("The mana can pay an activated ability's cost")
    void restrictedManaPaysEquipCost() {
        Permanent karolina = harness.addToBattlefieldAndReturn(player1, new KarolinaDeanRunaway());
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        harness.activateAbility(player1, 1, null, karolina.getId());
        resolveAllTriggers();

        assertThat(boots.getAttachedTo()).isEqualTo(karolina.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting from hand with ordinary mana leaves the restricted mana intact")
    void ordinaryManaCanPayForHandSpell() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new KarolinaDeanRunaway()));

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Karolina Dean, Runaway");
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger at the second main phase, and unused mana empties")
    void noSecondMainPhaseTrigger() {
        harness.addToBattlefield(player1, new KarolinaDeanRunaway());
        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
