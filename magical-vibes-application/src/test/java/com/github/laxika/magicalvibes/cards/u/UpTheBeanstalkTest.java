package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.b.BeanstalkWurm;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IngeniousProdigy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UpTheBeanstalk.class, CrawWurm.class, GrizzlyBears.class,
        BeanstalkWurm.class, Gingerbrute.class, IngeniousProdigy.class})
class UpTheBeanstalkTest extends BaseCardTest {

    @Test
    void drawsForManaValueExactlyFiveBeforeTheSpellResolves() {
        Gingerbrute drawn = new Gingerbrute();
        BeanstalkWurm spell = new BeanstalkWurm();
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
    }

    @Test
    void usesAdventureManaValueInsteadOfCreatureManaValue() {
        Gingerbrute drawn = new Gingerbrute();
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.setHand(player1, List.of(new BeanstalkWurm()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void includesChosenXInSpellManaValue() {
        Gingerbrute drawn = new Gingerbrute();
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.setHand(player1, List.of(new IngeniousProdigy()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotDrawForOpponentsExpensiveSpell() {
        Gingerbrute drawn = new Gingerbrute();
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new BeanstalkWurm()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsCardWhenItEnters() {
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new UpTheBeanstalk()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Draws a card when its controller casts a spell with mana value five or greater")
    void drawsForExpensiveSpell() {
        GrizzlyBears drawn = new GrizzlyBears();
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Does not trigger for a spell with mana value less than five")
    void doesNotTriggerForLowerManaValueSpell() {
        GrizzlyBears drawn = new GrizzlyBears();
        harness.addToBattlefield(player1, new UpTheBeanstalk());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }
}
