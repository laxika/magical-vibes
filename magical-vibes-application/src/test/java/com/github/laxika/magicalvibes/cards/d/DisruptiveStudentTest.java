package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptiveStudent.class, CoralMerfolk.class, DarkRitual.class})
class DisruptiveStudentTest extends BaseCardTest {

    @Test
    void countersSpellWhenControllerCannotPay() {
        Permanent student = addCreatureReady(player1, new DisruptiveStudent());

        harness.forceActivePlayer(player2);
        CoralMerfolk merfolk = new CoralMerfolk();
        harness.castFromHand(player2, merfolk, "{1}{U}");

        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Coral Merfolk");
        assertThat(student.isTapped()).isTrue();
    }

    @Test
    void spellResolvesWhenControllerPays() {
        Permanent student = addCreatureReady(player1, new DisruptiveStudent());

        harness.forceActivePlayer(player2);
        CoralMerfolk merfolk = new CoralMerfolk();
        harness.castFromHand(player2, merfolk, "{1}{U}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(student.isTapped()).isTrue();
    }

    @Test
    void spellIsCounteredWhenControllerDeclinesToPay() {
        addCreatureReady(player1, new DisruptiveStudent());

        harness.forceActivePlayer(player2);
        CoralMerfolk merfolk = new CoralMerfolk();
        harness.castFromHand(player2, merfolk, "{1}{U}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Coral Merfolk");
    }

    @Test
    void countersNoncreatureSpell() {
        addCreatureReady(player1, new DisruptiveStudent());

        harness.forceActivePlayer(player2);
        DarkRitual ritual = new DarkRitual();
        harness.castFromHand(player2, ritual, "{B}");
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, ritual.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dark Ritual");
    }

    @Test
    void cannotTargetPermanent() {
        addCreatureReady(player1, new DisruptiveStudent());
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
