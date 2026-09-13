package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PhyrexianProwler;
import com.github.laxika.magicalvibes.cards.s.SkyshroudRidgeback;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RathiFiend.class, RathiIntimidator.class, SkyshroudRidgeback.class, PhyrexianProwler.class})
class RathiFiendTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player lose 3 life")
    void etbMakesEachPlayerLoseThreeLife() {
        harness.setHand(player1, List.of(new RathiFiend()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Activated ability puts a qualifying Mercenary permanent onto the battlefield")
    void searchesMercenaryPermanentWithManaValueAtMostThree() {
        var fiend = addCreatureReady(player1, new RathiFiend());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new RathiIntimidator(), new SkyshroudRidgeback(), new PhyrexianProwler()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Rathi Intimidator");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(fiend.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Rathi Intimidator");
        harness.assertNotOnBattlefield(player1, "Skyshroud Ridgeback");
        harness.assertNotOnBattlefield(player1, "Phyrexian Prowler");
    }
}
