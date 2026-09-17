package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LastMarchOfTheEnts.class, Counterspell.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class LastMarchOfTheEntsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws based on the greatest toughness and puts any number of creatures from hand onto the battlefield")
    void drawsAndPutsCreaturesFromHandOntoBattlefield() {
        SerraAngel battlefieldAngel = new SerraAngel();
        harness.addToBattlefield(player1, battlefieldAngel);

        LastMarchOfTheEnts spell = new LastMarchOfTheEnts();
        GrizzlyBears bears = new GrizzlyBears();
        SerraAngel handAngel = new SerraAngel();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(spell, bears, handAngel, forest));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), handAngel.getId())
                .doesNotContain(forest.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), handAngel.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsOnly("Forest").hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .contains(battlefieldAngel, bears, handAngel);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player1, new SerraAngel());

        LastMarchOfTheEnts spell = new LastMarchOfTheEnts();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 8);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(counterspell);
    }
}
