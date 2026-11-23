package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.s.StabWound;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerforatorCrocodile.class, HillGiant.class, StabWound.class, ThayanEvokers.class, WhiteKnight.class})
class PerforatorCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures a Stab Wound attached to each creature an opponent controls")
    void conjuresAndAttachesStabWoundToEachOpposingCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new PerforatorCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> wounds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Stab Wound"))
                .toList();
        assertThat(wounds).hasSize(2);
        assertThat(wounds).allMatch(Permanent::isAttached);
        assertThat(wounds).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(firstOpponentCreature.getId(), secondOpponentCreature.getId());
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, firstOpponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, secondOpponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PerforatorCrocodile()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Perforator Crocodile");
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The Stab Wounds are one conjure event")
    void conjuringAStabWoundForEachOpposingCreatureTriggersConjureAbilitiesOnce() {
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new PerforatorCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Stab Wound"))
                .hasSize(2);
        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("No Stab Wound is conjured for a creature it can't enchant, and none reaches a graveyard")
    void noStabWoundForACreatureItCannotEnchant() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new WhiteKnight());

        harness.setHand(player1, List.of(new PerforatorCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Stab Wound"))
                .singleElement()
                .satisfies(wound -> {
                    assertThat(wound.getAttachedTo()).isEqualTo(bears.getId());
                    assertThat(wound.getCard().getSetCode()).isEqualTo("RTR");
                    assertThat(wound.getCard().isConjured()).isTrue();
                });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Stab Wound"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight);
        assertThat(gameLogContains("conjures Stab Wound onto the battlefield attached to Hill Giant.")).isTrue();
    }
}
