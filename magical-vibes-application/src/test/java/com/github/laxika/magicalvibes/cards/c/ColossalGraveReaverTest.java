package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossalGraveReaver.class, Forest.class, GrizzlyBears.class})
class ColossalGraveReaverTest extends BaseCardTest {

    @Test
    void entersAndReturnsOneCreatureMilledThisWay() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card otherCreature = new GrizzlyBears();
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(creature, land, otherCreature));

        harness.enterBattlefieldAndReturn(player1, new ColossalGraveReaver());
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(creature, otherCreature);

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(otherCreature));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldCreature, creature, land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == otherCreature);
    }

    @Test
    void attackingMillsThreeCardsAndReturnsACreatureFromThatMillEvent() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, land, secondCreature));
        Permanent reaver = addCreatureReady(player1, new ColossalGraveReaver());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(reaver)));
        resolveAllTriggers();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(creature, secondCreature);
        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, secondCreature);
    }
}
