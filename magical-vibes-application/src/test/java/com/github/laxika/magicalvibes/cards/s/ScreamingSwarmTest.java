package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamingSwarm.class, GrizzlyBears.class})
class ScreamingSwarmTest extends BaseCardTest {

    @Test
    void millsOneCardPerAttackingCreature() {
        addReadyCreature(player1, new ScreamingSwarm());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        harness.setLibrary(player2, libraryWithCards(10));

        declareAttackers(player1, List.of(1, 2));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void canTargetYourself() {
        addReadyCreature(player1, new ScreamingSwarm());
        addReadyCreature(player1, new GrizzlyBears());
        harness.setLibrary(player1, libraryWithCards(5));

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void graveyardAbilityPutsThisCardSecondFromTop() {
        ScreamingSwarm swarm = new ScreamingSwarm();
        harness.setGraveyard(player1, List.of(swarm));
        harness.setLibrary(player1, libraryWithCards(3));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(1)).isSameAs(swarm);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(swarm);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private List<Card> libraryWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
