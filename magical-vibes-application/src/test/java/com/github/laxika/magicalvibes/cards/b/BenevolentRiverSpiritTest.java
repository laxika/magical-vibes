package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenevolentRiverSpirit.class, GrizzlyBears.class})
class BenevolentRiverSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps five creatures while casting")
    void waterbendTapsFiveCreatures() {
        List<Permanent> creatures = addCreatures(5);
        prepareCast(ManaColor.BLUE, 2);

        harness.castCreatureTappingPermanents(player1, 0, idsOf(creatures));

        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Available mana reduces the number of permanents tapped for waterbend")
    void availableManaReducesWaterbendTaps() {
        List<Permanent> creatures = addCreatures(4);
        harness.setHand(player1, List.of(new BenevolentRiverSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreatureTappingPermanents(player1, 0, idsOf(creatures));

        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Entering the battlefield scries two cards")
    void entersAndScriesTwo() {
        List<Permanent> creatures = addCreatures(5);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        prepareCast(ManaColor.BLUE, 2);

        harness.castCreatureTappingPermanents(player1, 0, idsOf(creatures));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Benevolent River Spirit");

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private List<Permanent> addCreatures(int count) {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        }
        return creatures;
    }

    private void prepareCast(ManaColor color, int amount) {
        harness.setHand(player1, List.of(new BenevolentRiverSpirit()));
        harness.addMana(player1, color, amount);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private List<UUID> idsOf(List<Permanent> permanents) {
        return permanents.stream().map(Permanent::getId).toList();
    }
}
