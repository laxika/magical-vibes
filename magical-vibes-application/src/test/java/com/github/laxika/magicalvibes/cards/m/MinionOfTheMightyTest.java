package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinionOfTheMighty.class, DragonWhelp.class, GrizzlyBears.class})
class MinionOfTheMightyTest extends BaseCardTest {

    @Test
    @DisplayName("Pack tactics puts a Dragon from hand onto the battlefield tapped and attacking")
    void putsDragonTappedAndAttacking() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWhelp()));

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent dragon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DragonWhelp)
                .findFirst()
                .orElseThrow();
        assertThat(dragon.isTapped()).isTrue();
        assertThat(dragon.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Pack tactics offers only Dragon creature cards")
    void offersOnlyDragonCreatures() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears(), new DragonWhelp()));

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Pack tactics does not trigger below total attacking power six")
    void doesNotTriggerBelowThreshold() {
        addCreatureReady(player1, new MinionOfTheMighty());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonWhelp()));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
