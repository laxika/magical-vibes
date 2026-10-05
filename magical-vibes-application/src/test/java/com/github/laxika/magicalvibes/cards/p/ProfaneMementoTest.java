package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfaneMemento.class, GrizzlyBears.class, Ornithopter.class, Shock.class, TomeScour.class,
        MindRot.class, Cancel.class, RaiseTheAlarm.class})
class ProfaneMementoTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 1 life when an opponent's creature dies")
    void gainsLifeWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player2, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID thopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castAndResolveInstant(player1, 0, thopterId);
        harness.passBothPriorities(); // Life-gain trigger resolves

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Triggers for each creature card milled into an opponent's graveyard")
    void gainsLifePerMilledCreatureCard() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // first trigger
        harness.passBothPriorities(); // second trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature is put into their graveyard")
    void doesNotTriggerForOwnCreature() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player1, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID thopterId = harness.getPermanentId(player1, "Ornithopter");
        harness.castAndResolveInstant(player1, 0, thopterId);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature card put into an opponent's graveyard")
    void doesNotTriggerForNoncreatureCard() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.setLibrary(player2, List.of(new TomeScour(), new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Each discarded creature card triggers life gain")
    void gainsLifeForDiscardedCreatureCards() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        Ornithopter first = new Ornithopter();
        Ornithopter second = new Ornithopter();
        harness.setHand(player2, List.of(first, second));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("A countered creature spell triggers life gain from the stack")
    void gainsLifeForCounteredCreatureSpell() {
        harness.addToBattlefield(player2, new ProfaneMemento());
        Ornithopter creature = new Ornithopter();
        harness.setHand(player2, List.of(new Cancel()));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Creature tokens dying do not trigger life gain")
    void doesNotTriggerForCreatureToken() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.setHand(player2, List.of(new RaiseTheAlarm()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        UUID soldierId = harness.getPermanentId(player2, "Soldier");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, soldierId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(soldierId));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Each Memento triggers independently for the same creature card")
    void multipleMementosEachGainLife() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Ornithopter"));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }
}
