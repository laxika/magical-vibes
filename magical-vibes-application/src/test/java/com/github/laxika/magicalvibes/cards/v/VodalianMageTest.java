package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianMage.class, RiverMerfolk.class})
class VodalianMageTest extends BaseCardTest {

    @Test
    void countersSpellWhenControllerCannotPay() {
        Permanent mage = addCreatureReady(player1, new VodalianMage());

        harness.forceActivePlayer(player2);
        RiverMerfolk merfolk = new RiverMerfolk();
        harness.castFromHand(player2, merfolk, "{U}{U}");

        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "River Merfolk");
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    void spellResolvesWhenControllerPays() {
        Permanent mage = addCreatureReady(player1, new VodalianMage());

        harness.forceActivePlayer(player2);
        RiverMerfolk merfolk = new RiverMerfolk();
        harness.castFromHand(player2, merfolk, "{U}{U}");
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "River Merfolk");
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    void spellIsCounteredWhenControllerDeclinesToPay() {
        addCreatureReady(player1, new VodalianMage());

        harness.forceActivePlayer(player2);
        RiverMerfolk merfolk = new RiverMerfolk();
        harness.castFromHand(player2, merfolk, "{U}{U}");
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "River Merfolk");
    }

    @Test
    void cannotActivateWhenTapped() {
        Permanent mage = addCreatureReady(player1, new VodalianMage());
        mage.tap();

        harness.forceActivePlayer(player2);
        RiverMerfolk merfolk = new RiverMerfolk();
        harness.castFromHand(player2, merfolk, "{U}{U}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutSpellTarget() {
        addCreatureReady(player1, new VodalianMage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        addCreatureReady(player1, new VodalianMage());

        harness.forceActivePlayer(player2);
        RiverMerfolk spell = new RiverMerfolk();
        harness.castFromHand(player2, spell, "{U}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPermanent() {
        addCreatureReady(player1, new VodalianMage());
        Permanent permanent = addCreatureReady(player2, new RiverMerfolk());

        harness.forceActivePlayer(player2);
        RiverMerfolk spell = new RiverMerfolk();
        harness.castFromHand(player2, spell, "{U}{U}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
