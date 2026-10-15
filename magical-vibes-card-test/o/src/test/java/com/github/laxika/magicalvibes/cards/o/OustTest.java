package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.h.HedronMatrix;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Oust.class, GlorySeeker.class, Island.class, HedronMatrix.class})
class OustTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target creature second from the top and its controller gains 3 life")
    void putsCreatureSecondFromTopAndControllerGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard, new Island(), new Island()));
        harness.setLife(player2, 10);

        harness.setHand(player1, List.of(new Oust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(0)).isSameAs(topCard);
        assertThat(library.get(1).getName()).isEqualTo("Glory Seeker");
        harness.assertLife(player2, 13);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HedronMatrix());
        harness.setHand(player1, List.of(new Oust()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles without life gain if the target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Oust()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player1, "Oust");
    }

    @Test
    @DisplayName("Puts the creature on top when its owner's library is empty")
    void putsCreatureIntoEmptyLibrary() {
        Card card = new GlorySeeker();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Oust()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card);
        harness.assertLife(player2, 23);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target your own creature and gives you the life")
    void canTargetOwnCreature() {
        Card card = new GlorySeeker();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Oust()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, card);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner's library but gives life to its controller")
    void returnsStolenCreatureToOwnerAndGivesLifeToController() {
        Card card = new GlorySeeker();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(creature.getId(), player1.getId());
        Card ownerTopCard = new Island();
        Card controllerTopCard = new Island();
        harness.setLibrary(player1, List.of(ownerTopCard));
        harness.setLibrary(player2, List.of(controllerTopCard));
        harness.setHand(player1, List.of(new Oust()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownerTopCard, card);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerTopCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }
}
