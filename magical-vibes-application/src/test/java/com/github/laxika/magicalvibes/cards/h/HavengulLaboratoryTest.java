package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavengulLaboratory.class, HavengulMystery.class, GrizzlyBears.class, Forest.class})
class HavengulLaboratoryTest extends BaseCardTest {

    @Test
    void paysFourManaToInvestigate() {
        Permanent laboratory = addLaboratory();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findClues(player1)).hasSize(1);
        assertThat(laboratory.isTapped()).isTrue();
    }

    @Test
    void transformsAtYourEndStepAfterSacrificingThreeClues() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(laboratory.isTransformed()).isTrue();
        assertThat(laboratory.getCard()).isInstanceOf(HavengulMystery.class);
    }

    @Test
    void transformsBackWhenTheReturnedCreatureLeaves() {
        Permanent laboratory = addLaboratory();
        investigate(laboratory, 3);
        sacrificeClues(player1, 3);
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        GrizzlyBears creature = new GrizzlyBears();
        Permanent creaturePermanent = harness.addToBattlefieldAndReturn(player1, creature);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creaturePermanent));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
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

    private void investigate(Permanent laboratory, int count) {
        for (int i = 0; i < count; i++) {
            harness.addMana(player1, ManaColor.COLORLESS, 4);
            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(laboratory), 1, null, null);
            harness.passBothPriorities();
            laboratory.untap();
        }
    }

    private void sacrificeClues(Player player, int count) {
        harness.setLibrary(player, List.of(new Forest(), new Forest(), new Forest()));
        for (int i = 0; i < count; i++) {
            Permanent clue = findClues(player).getFirst();
            harness.addMana(player, ManaColor.COLORLESS, 2);
            harness.activateAbility(player,
                    gd.playerBattlefields.get(player.getId()).indexOf(clue), 0, null, null);
            harness.passBothPriorities();
        }
    }

    private List<Permanent> findClues(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.CLUE))
                .toList();
    }

}
