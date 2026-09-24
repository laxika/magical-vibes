package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavengulLaboratory.class, HavengulMystery.class, GrizzlyBears.class})
class HavengulLaboratoryTest extends BaseCardTest {

    @Test
    @DisplayName("Paying four mana and tapping Havengul Laboratory creates a Clue")
    void investigates() {
        Permanent laboratory = addLaboratory();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(laboratory), 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(laboratory.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Three sacrificed Clues transform Havengul Laboratory at your end step")
    void transformsAfterThreeCluesAreSacrificed() {
        Permanent laboratory = addLaboratory();
        addClues(3);
        sacrificeClues(3);

        advanceToEndStep(player1);

        assertThat(laboratory.isTransformed()).isTrue();
        assertThat(laboratory.getCard()).isInstanceOf(HavengulMystery.class);
    }

    @Test
    @DisplayName("Havengul Mystery returns a creature and transforms back when it leaves")
    void returnsCreatureAndTransformsBackWhenItLeaves() {
        Permanent laboratory = addLaboratory();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addClues(3);
        sacrificeClues(3);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(laboratory.isTransformed()).isTrue();
        assertThat(laboratory.getChosenPermanentId()).isEqualTo(returned.getId());
    }

    @Test
    @DisplayName("Havengul Mystery transforms back when the returned creature leaves")
    void transformsBackWhenReturnedCreatureLeaves() {
        Permanent laboratory = addLaboratory();
        Permanent returned = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        laboratory.setCard(laboratory.getOriginalCard().getBackFaceCard());
        laboratory.setTransformed(true);
        laboratory.setChosenPermanentId(returned.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, returned));
        harness.passBothPriorities();

        assertThat(laboratory.isTransformed()).isFalse();
        assertThat(laboratory.getCard()).isInstanceOf(HavengulLaboratory.class);
    }

    private Permanent addLaboratory() {
        Permanent laboratory = harness.addToBattlefieldAndReturn(player1, new HavengulLaboratory());
        laboratory.setSummoningSick(false);
        return laboratory;
    }

    private void addClues(int count) {
        for (int i = 0; i < count; i++) {
            Card clue = new Card();
            clue.setName("Clue");
            clue.setType(CardType.ARTIFACT);
            clue.setManaCost("");
            clue.setToken(true);
            clue.setSubtypes(List.of(CardSubtype.CLUE));
            clue.addActivatedAbility(new ActivatedAbility(
                    false,
                    "{2}",
                    List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                    "{2}, Sacrifice this token: Draw a card."
            ));
            harness.addToBattlefield(player1, clue);
        }
    }

    private void sacrificeClues(int count) {
        for (int i = 0; i < count; i++) {
            int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanents(player1, "Clue").getFirst());
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, clueIndex, null, null);
            harness.passBothPriorities();
        }
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
