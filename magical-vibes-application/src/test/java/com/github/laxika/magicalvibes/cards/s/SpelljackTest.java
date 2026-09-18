package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spelljack.class, SuntailHawk.class, MentalNote.class, HaplessResearcher.class})
class SpelljackTest extends BaseCardTest {

    @Test
    void countersAndExilesTargetSpellWithFreeCastPermissionForItsController() {
        SuntailHawk hawk = new SuntailHawk();
        Spelljack spelljack = new Spelljack();
        harness.castFromHand(player1, hawk, "{W}");
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hawk.getId()));
        assertThat(gd.exilePlayPermissions.get(hawk.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(hawk.getId());
        harness.assertNotInGraveyard(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
    }

    @Test
    void controllerCanCastCounteredSpellFromExileWithoutMana() {
        SuntailHawk hawk = new SuntailHawk();
        Spelljack spelljack = new Spelljack();
        harness.castFromHand(player1, hawk, "{W}");
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, hawk.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.findExiledCard(hawk.getId())).isNull();
    }

    @Test
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersAndExilesNoncreatureSpell() {
        MentalNote mentalNote = new MentalNote();
        Spelljack spelljack = new Spelljack();
        harness.castFromHand(player1, mentalNote, "{U}");
        harness.setHand(player2, List.of(spelljack));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mentalNote.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(mentalNote.getId()));
        assertThat(gd.exilePlayPermissions.get(mentalNote.getId())).isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player1, "Mental Note");
    }

    @Test
    void cannotTargetActivatedAbility() {
        HaplessResearcher researcher = new HaplessResearcher();
        harness.addToBattlefield(player1, researcher);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player2, List.of(new Spelljack()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, researcher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
