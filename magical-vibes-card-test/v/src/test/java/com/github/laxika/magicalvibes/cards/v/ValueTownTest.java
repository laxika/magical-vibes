package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TakeATripTo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValueTown.class, TakeATripTo.class, Forest.class})
class ValueTownTest extends BaseCardTest {

    @Test
    @DisplayName("Value Town enters tapped and produces a chosen blue or red mana")
    void entersTappedAndProducesChosenMana() {
        harness.setHand(player1, List.of(new ValueTown()));

        harness.playLand(player1, 0);
        Permanent valueTown = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(valueTown.isTapped()).isTrue();

        valueTown.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
    }

    @Test
    @DisplayName("Take a Trip to draws two cards, damages each opponent, and exiles Value Town")
    void adventureDrawsDamagesAndExiles() {
        ValueTown valueTown = new ValueTown();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(valueTown));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.findExiledCard(valueTown.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(valueTown.getId())).isEqualTo(player1.getId());
    }

    @Test
    void producesBlueManaAndTapsWithoutUsingTheStack() {
        Permanent town = harness.addToBattlefieldAndReturn(player1, new ValueTown());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(town.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landCanBePlayedTappedFromAdventureExile() {
        ValueTown town = resolveAdventure();

        harness.castFromExile(player1, town.getId());

        assertThat(findPermanent(player1, "Value Town").isTapped()).isTrue();
        assertThat(gd.findExiledCard(town.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(town.getId());
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isOne();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureLandCannotBePlayedAfterUsingTheLandPlay() {
        ValueTown town = resolveAdventure();
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, town.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(town.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(town.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureLandCannotBePlayedDuringCombat() {
        ValueTown town = resolveAdventure();
        gd.currentStep = TurnStep.BEGINNING_OF_COMBAT;

        assertThatThrownBy(() -> harness.castFromExile(player1, town.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(town.getId())).isNotNull();
    }

    @Test
    void ordinaryExileDoesNotAllowPlayingTheLand() {
        ValueTown town = new ValueTown();
        harness.setExile(player1, List.of(town));

        assertThatThrownBy(() -> harness.castFromExile(player1, town.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(town.getId())).isNotNull();
    }

    private ValueTown resolveAdventure() {
        ValueTown town = new ValueTown();
        harness.setHand(player1, List.of(town));
        harness.setLibrary(player1, List.of(new ValueTown(), new ValueTown(), new ValueTown()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        return town;
    }
}
