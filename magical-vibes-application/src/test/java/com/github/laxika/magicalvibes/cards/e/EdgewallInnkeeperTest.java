package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BoulderRush;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WildwoodTracker;
import com.github.laxika.magicalvibes.cards.r.RimrockKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EdgewallInnkeeper.class, RimrockKnight.class, BoulderRush.class, WildwoodTracker.class, Forest.class})
class EdgewallInnkeeperTest extends BaseCardTest {

    @Test
    void drawsWhenCreatureFaceOfAdventureCardIsCast() {
        seedDeck();
        harness.addToBattlefield(player1, new EdgewallInnkeeper());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawForNonAdventureCreatureOrAdventureFace() {
        seedDeck();
        harness.addToBattlefield(player1, new EdgewallInnkeeper());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WildwoodTracker());
        harness.setHand(player1, List.of(new WildwoodTracker()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        seedDeck();
        RimrockKnight rimrockKnight = new RimrockKnight();
        harness.setHand(player1, List.of(rimrockKnight));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenCreatureIsCastFromExileAfterAdventure() {
        seedDeck();
        Permanent innkeeper = harness.addToBattlefieldAndReturn(player1, new EdgewallInnkeeper());
        RimrockKnight knight = new RimrockKnight();
        harness.setHand(player1, List.of(knight));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, innkeeper.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, knight.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rimrock Knight");
    }

    @Test
    void doesNotDrawForOpponentsAdventureCreature() {
        seedDeck();
        harness.addToBattlefield(player2, new EdgewallInnkeeper());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void eachInnkeeperDrawsAndTriggerSurvivesSourceLeaving() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new EdgewallInnkeeper());
        harness.addToBattlefield(player1, new EdgewallInnkeeper());
        harness.setHand(player1, List.of(new RimrockKnight()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rimrock Knight");
    }

    private void seedDeck() {
        harness.setLibrary(player1, List.of(new Forest()));
    }
}
