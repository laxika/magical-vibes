package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpotterThopter.class, Forest.class, Island.class})
class SpotterThopterTest extends BaseCardTest {

    @Test
    void normalCastScriesForPrintedPower() {
        harness.setHand(player1, List.of(new SpotterThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(4);
    }

    @Test
    void prototypeCastScriesForPrototypePower() {
        harness.setHand(player1, List.of(new SpotterThopter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    void prototypeScryIsLimitedByPowerRatherThanLibrarySize() {
        harness.setHand(player1, List.of(new SpotterThopter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void scryUsesPowerAtResolution() {
        harness.setHand(player1, List.of(new SpotterThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(),
                new Island(), new Forest(), new Island()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().setPowerModifier(2);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(6);
    }

    @Test
    void zeroPowerDoesNotStartScryInteraction() {
        harness.setHand(player1, List.of(new SpotterThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().setPowerModifier(-4);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
