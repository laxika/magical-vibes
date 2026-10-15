package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TreasureCruise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KheruSpellsnatcher.class, GrizzlyBears.class, TreasureCruise.class})
class KheruSpellsnatcherTest extends BaseCardTest {

    @Test
    void turningFaceUpCountersSpellExilesItAndGrantsControllerFreeCastPermission() {
        KheruSpellsnatcher kheru = new KheruSpellsnatcher();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(kheru));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent faceDownKheru = findPermanent(player1, "Kheru Spellsnatcher");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(faceDownKheru));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    void canCounterOwnSpellAndCastItAgainWithoutMana() {
        harness.setHand(player1, List.of(new KheruSpellsnatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        harness.clearPriorityPassed();
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    void freeCastingPermissionAllowsDelveSpellButPreservesSorceryTiming() {
        harness.setHand(player1, List.of(new KheruSpellsnatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        TreasureCruise cruise = new TreasureCruise();
        harness.castFromHand(player2, cruise, "{7}{U}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, 0);
        harness.handlePermanentChosen(player1, cruise.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(cruise.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, cruise.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(cruise.getId())).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, cruise.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(cruise.getId()));
        assertThat(gd.findExiledCard(cruise.getId())).isNull();
    }

    @Test
    void turningFaceUpWithoutSpellDoesNotCreateTargetChoice() {
        harness.setHand(player1, List.of(new KheruSpellsnatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent faceDownKheru = findPermanent(player1, "Kheru Spellsnatcher");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(faceDownKheru));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
