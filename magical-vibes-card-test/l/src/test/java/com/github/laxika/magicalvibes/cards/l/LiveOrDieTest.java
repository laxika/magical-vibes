package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiveOrDie.class, GrizzlyBears.class, HolyDay.class, Millstone.class})
class LiveOrDieTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureFromGraveyardToBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotReturnNonCreatureCardFromGraveyard() {
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotDestroyNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                harness.getPermanentId(player2, "Millstone")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsOnlyTheChosenCreatureAndDoesNotDestroyAnotherCreature() {
        Card chosen = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 0, chosen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(chosen.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(other.getId())
                .doesNotContain(chosen.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNotChooseAnotherCreatureIfGraveyardTargetLeavesBeforeResolution() {
        Card chosen = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 0, chosen.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(chosen));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Live or Die");
    }

    @Test
    void canDestroyOwnCreatureWithoutReturningAnotherCreature() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LiveOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(graveyardCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof GrizzlyBears))
                .hasSize(2);
    }
}
