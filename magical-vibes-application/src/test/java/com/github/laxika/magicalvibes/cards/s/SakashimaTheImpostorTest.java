package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.ONaginata;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakashimaTheImpostor.class, ShinenOfStarsLight.class, ONaginata.class})
class SakashimaTheImpostorTest extends BaseCardTest {

    @Test
    @DisplayName("Sakashima copies an opposing creature while keeping its name, legendary supertype, and return ability")
    void copiesCreatureWithSakashimaExceptions() {
        Permanent shinen = harness.addToBattlefieldAndReturn(player2, new ShinenOfStarsLight());
        castSakashima();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shinen.getId());
        Permanent sakashima = findPermanent(player1, "Sakashima the Impostor");

        assertThat(sakashima.getCard().getName()).isEqualTo("Sakashima the Impostor");
        assertThat(sakashima.getCard().getPower()).isEqualTo(2);
        assertThat(sakashima.getCard().getToughness()).isEqualTo(1);
        assertThat(sakashima.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(sakashima.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Sakashima remains itself when its copy choice is declined")
    void remainsSakashimaWhenCopyChoiceIsDeclined() {
        harness.addToBattlefield(player2, new ShinenOfStarsLight());
        castSakashima();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent sakashima = findPermanent(player1, "Sakashima the Impostor");
        assertThat(sakashima.getCard().getPower()).isEqualTo(3);
        assertThat(sakashima.getCard().getToughness()).isEqualTo(1);
        assertThat(sakashima.getCard().getActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Sakashima does not copy a noncreature permanent")
    void doesNotCopyNoncreaturePermanent() {
        harness.addToBattlefield(player2, new ONaginata());
        castSakashima();

        harness.passBothPriorities();

        Permanent sakashima = findPermanent(player1, "Sakashima the Impostor");
        assertThat(sakashima.getCard().getPower()).isEqualTo(3);
        assertThat(sakashima.getCard().getToughness()).isEqualTo(1);
        assertThat(sakashima.getCard().getActivatedAbilities()).isEmpty();
        harness.assertOnBattlefield(player2, "O-Naginata");
    }

    @Test
    @DisplayName("Sakashima's copied return ability returns it at the next end step")
    void returnsAtNextEndStep() {
        Permanent shinen = harness.addToBattlefieldAndReturn(player1, new ShinenOfStarsLight());
        SakashimaTheImpostor card = castSakashima();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shinen.getId());
        Permanent sakashima = findPermanent(player1, "Sakashima the Impostor");

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
        harness.castFromHand(player1, card, "{2}{U}{U}");
        return card;
    }
}
