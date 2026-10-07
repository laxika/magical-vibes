package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestlessAnchorage;
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

@CardUsed({TithingBlade.class, ConsumingSepulcher.class, GrizzlyBears.class, RestlessAnchorage.class})
class TithingBladeTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent sacrifices a creature of their choice")
    void eachOpponentSacrificesCreatureOnEnter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new TithingBlade(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, secondCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Craft exiles a creature and returns Consuming Sepulcher transformed")
    void craftsWithCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new TithingBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blade, creature);
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof ConsumingSepulcher);
    }

    @Test
    @DisplayName("Craft exiles a creature card from the graveyard and returns transformed")
    void craftsWithCreatureFromGraveyard() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new TithingBlade());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blade);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof ConsumingSepulcher);
    }

    @Test
    @DisplayName("Consuming Sepulcher drains each opponent during its controller's upkeep")
    void consumingSepulcherUpkeepDrainsOpponent() {
        addTransformedSepulcher();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The controller sacrifices nothing when the opponent has no creatures")
    void noOpponentCreaturesDoesNotSacrificeControllersCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new TithingBlade(), "{1}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Tithing Blade");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Craft pays both exile costs before the ability resolves")
    void craftExilesSourceAndMaterialAsCosts() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new TithingBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blade, creature);
        assertThat(gd.findExiledCard(blade.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Consuming Sepulcher");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Consuming Sepulcher");
        assertThat(gd.findExiledCard(blade.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Craft cannot use an opponent's creature as its material")
    void craftRejectsOpponentsCreature() {
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new TithingBlade());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blade);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Craft cannot be activated during upkeep")
    void craftRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new TithingBlade());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tithing Blade");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Consuming Sepulcher does not trigger during an opponent's upkeep")
    void consumingSepulcherDoesNotTriggerOnOpponentsUpkeep() {
        addTransformedSepulcher();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An animated Restless Anchorage is a legal creature to craft with")
    void craftsWithAnimatedLand() {
        harness.addToBattlefield(player1, new TithingBlade());
        Permanent anchorage = harness.addToBattlefieldAndReturn(player1, new RestlessAnchorage());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, anchorage)).isTrue();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(anchorage.getCard().getId())).isNotNull();
        harness.assertOnBattlefield(player1, "Consuming Sepulcher");
        harness.assertNotOnBattlefield(player1, "Tithing Blade");
    }

    private void addTransformedSepulcher() {
        TithingBlade front = new TithingBlade();
        Permanent sepulcher = harness.addToBattlefieldAndReturn(player1, front);
        sepulcher.setCard(front.getBackFaceCard());
        sepulcher.setTransformed(true);
    }
}
