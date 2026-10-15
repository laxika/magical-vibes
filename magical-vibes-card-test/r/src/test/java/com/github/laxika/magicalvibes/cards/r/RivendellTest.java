package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KefnetTheMindful;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rivendell.class, KefnetTheMindful.class, GrizzlyBears.class})
class RivendellTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a legendary creature")
    void entersTappedWithoutLegendaryCreature() {
        harness.setHand(player1, List.of(new Rivendell()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rivendell").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a legendary creature")
    void entersUntappedWithLegendaryCreature() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Rivendell()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rivendell").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Scry 2 ability requires a legendary creature")
    void scryAbilityRequiresLegendaryCreature() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Scry 2 ability opens a scry interaction")
    void scryAbilityOpensInteraction() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void nonlegendaryCreatureDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Rivendell()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rivendell").isTapped()).isTrue();
    }

    @Test
    void opponentsLegendaryCreatureDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Rivendell()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rivendell").isTapped()).isTrue();
    }

    @Test
    void opponentsLegendaryCreatureDoesNotAllowScryActivation() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player2, new KefnetTheMindful());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Rivendell").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesBlueManaWithoutLegendaryCreature() {
        harness.addToBattlefield(player1, new Rivendell());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Rivendell").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryResolvesAfterLegendaryCreatureLeavesBattlefield() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player1, new KefnetTheMindful());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findPermanent(player1, "Rivendell").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Kefnet the Mindful"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void scryRequiresBothManaAndTapCosts() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        findPermanent(player1, "Rivendell").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scriesAvailableCardWhenLibraryContainsOnlyOneCard() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player1, new KefnetTheMindful());
        GrizzlyBears onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
    }
}
