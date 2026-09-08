package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BackForSeconds.class, DarksteelRelic.class, GrizzlyBears.class, YavimayaWurm.class})
class BackForSecondsTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCreatureCardsToHand() {
        Card bears = new GrizzlyBears();
        Card wurm = new YavimayaWurm();
        harness.setGraveyard(player1, List.of(bears, wurm));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), wurm.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), wurm.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Yavimaya Wurm");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Yavimaya Wurm");
    }

    @Test
    void bargainedCastMayPutOneLowManaValueCreatureOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card wurm = new YavimayaWurm();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setGraveyard(player1, List.of(bears, wurm));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), wurm.getId()));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Yavimaya Wurm");
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Back for Seconds");
    }

    @Test
    void bargainedCastCanDeclineBattlefieldReplacement() {
        Card bears = new GrizzlyBears();
        Card wurm = new YavimayaWurm();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setGraveyard(player1, List.of(bears, wurm));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), wurm.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Yavimaya Wurm");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
