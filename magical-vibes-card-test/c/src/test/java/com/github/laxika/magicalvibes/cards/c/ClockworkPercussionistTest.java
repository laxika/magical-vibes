package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClockworkPercussionist.class, Forest.class, WrathOfGod.class})
class ClockworkPercussionistTest extends BaseCardTest {

    @Test
    void whenItDiesExilesTopCardAndGrantsPlayPermissionUntilNextTurn() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new ClockworkPercussionist());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(topCard.getId());
    }

    @Test
    void deathTriggerUsesTheCreaturesController() {
        Forest topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player2, new ClockworkPercussionist());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player2.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainValue(player1.getId());
    }

    @Test
    void canPlayTheExiledLand() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefieldAndReturn(player1, new ClockworkPercussionist()).setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void exiledSpellRequiresItsNormalManaCost() {
        ClockworkPercussionist topCard = new ClockworkPercussionist();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefieldAndReturn(player1, new ClockworkPercussionist()).setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Clockwork Percussionist");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void emptyLibraryDoesNotGrantPlayPermission() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefieldAndReturn(player1, new ClockworkPercussionist()).setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Clockwork Percussionist");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void permissionLastsThroughTheControllersNextTurnAndThenExpires() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new ClockworkPercussionist()).setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }
}
