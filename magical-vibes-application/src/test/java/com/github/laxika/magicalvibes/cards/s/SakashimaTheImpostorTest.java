package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SakashimaTheImpostorTest extends BaseCardTest {

    @Test
    @DisplayName("Sakashima copies a creature while keeping its name, legendary supertype, and return ability")
    void copiesCreatureWithSakashimaExceptions() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSakashima();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent sakashima = findSakashima(bears);

        assertThat(sakashima.getCard().getName()).isEqualTo("Sakashima the Impostor");
        assertThat(sakashima.getCard().getPower()).isEqualTo(2);
        assertThat(sakashima.getCard().getToughness()).isEqualTo(2);
        assertThat(sakashima.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(sakashima.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Sakashima's copied return ability returns it at the next end step")
    void returnsAtNextEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SakashimaTheImpostor card = castSakashima();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent sakashima = findSakashima(bears);

        int sakashimaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sakashima);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, sakashimaIndex, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sakashima);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    private SakashimaTheImpostor castSakashima() {
        SakashimaTheImpostor card = new SakashimaTheImpostor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        return card;
    }

    private Permanent findSakashima(Permanent copiedCreature) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != copiedCreature)
                .findFirst()
                .orElseThrow();
    }
}
