package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Soulmender;
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

@CardUsed({BilboBirthdayCelebrant.class, Soulmender.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class BilboBirthdayCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one life to each life-gain event")
    void addsOneLifeToGainEvent() {
        harness.addToBattlefield(player1, new BilboBirthdayCelebrant());
        Permanent soulmender = harness.addToBattlefieldAndReturn(player1, new Soulmender());
        soulmender.setSummoningSick(false);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("At 111 life, exiles itself and puts any number of creature cards onto the battlefield")
    void searchesForAnyNumberOfCreaturesAtLifeThreshold() {
        Permanent bilbo = harness.addToBattlefieldAndReturn(player1, new BilboBirthdayCelebrant());
        bilbo.setSummoningSick(false);
        harness.setLife(player1, 110);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you have 111 or more life");

        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest, elves));
        harness.setLife(player1, 111);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(bears, elves);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bilbo.getCard());

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
