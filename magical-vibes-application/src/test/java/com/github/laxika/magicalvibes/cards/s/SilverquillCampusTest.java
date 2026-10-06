package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WitherbloomCampus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverquillCampus.class, WitherbloomCampus.class})
class SilverquillCampusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SilverquillCampus()));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent campus = findPermanent(player1, "Silverquill Campus");
        assertThat(campus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds white or black mana")
    void tappingAddsChosenMana() {
        Permanent whiteCampus = addReadyCampus();
        Permanent blackCampus = addReadyCampus();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(whiteCampus.isTapped()).isTrue();
        assertThat(blackCampus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana and tapping scries one")
    void paidAbilityScriesOne() {
        Permanent campus = addReadyCampus();
        harness.setLibrary(player1, List.of(new WitherbloomCampus()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(campus.isTapped()).isTrue();
    }

    private Permanent addReadyCampus() {
        return addCreatureReady(player1, new SilverquillCampus());
    }

    @Test
    void scryCanKeepTheTopCard() {
        addReadyCampus();
        SilverquillCampus top = new SilverquillCampus();
        WitherbloomCampus bottom = new WitherbloomCampus();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryCanPutTheTopCardOnTheBottom() {
        addReadyCampus();
        SilverquillCampus top = new SilverquillCampus();
        WitherbloomCampus bottom = new WitherbloomCampus();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutAChoice() {
        Permanent campus = addReadyCampus();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(campus.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void scryCannotBeActivatedWithOnlyThreeMana() {
        Permanent campus = addReadyCampus();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(campus.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void tappedCampusCannotActivateEitherAbility() {
        Permanent campus = addReadyCampus();
        campus.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }
}
