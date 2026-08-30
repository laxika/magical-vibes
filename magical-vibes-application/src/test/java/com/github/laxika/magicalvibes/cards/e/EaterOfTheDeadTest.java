package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EaterOfTheDead.class, Forest.class, GrizzlyBears.class})
class EaterOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("When tapped, Eater of the Dead exiles a creature card and untaps")
    void tappedEaterExilesCreatureAndUntaps() {
        Permanent eater = addReadyEater();
        eater.tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The ability can be activated while Eater of the Dead is untapped")
    void untappedEaterCanActivateButDoesNothing() {
        Permanent eater = addReadyEater();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        harness.activateAbility(player1, indexOf(eater), 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(eater.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature card")
    void rejectsNonCreatureTarget() {
        Permanent eater = addReadyEater();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(eater), 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEater() {
        Permanent eater = harness.addToBattlefieldAndReturn(player1, new EaterOfTheDead());
        eater.setSummoningSick(false);
        return eater;
    }

    private int indexOf(Permanent eater) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(eater);
    }
}
