package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({UnexpectedlyAbsent.class, GrizzlyBears.class, Forest.class})
class UnexpectedlyAbsentTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target nonland permanent beneath the top X cards of its owner's library")
    void putsTargetBeneathTopXCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new UnexpectedlyAbsent()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent -> permanent.getId().equals(target.getId()));
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(0).getName()).isEqualTo("Forest");
        assertThat(library.get(1).getName()).isEqualTo("Forest");
        assertThat(library.get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land permanent")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UnexpectedlyAbsent()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X zero puts a permanent you control on top without changing library order")
    void zeroPutsOwnPermanentOnTop() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new UnexpectedlyAbsent()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, first, second);
        harness.assertInGraveyard(player1, "Unexpectedly Absent");
    }

    @Test
    @DisplayName("X larger than the library puts the target on the bottom")
    void oversizedXPutsTargetOnBottom() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new UnexpectedlyAbsent()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, 5, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, creature);
    }

    @Test
    @DisplayName("A target can be put into an empty library even with positive X")
    void putsTargetIntoEmptyLibrary() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new UnexpectedlyAbsent()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("A stolen permanent goes to its owner's library rather than its controller's")
    void putsStolenPermanentIntoOwnersLibrary() {
        GrizzlyBears creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Forest ownersTopCard = new Forest();
        Forest controllersTopCard = new Forest();
        harness.setLibrary(player2, List.of(ownersTopCard));
        harness.setLibrary(player1, List.of(controllersTopCard));
        harness.setHand(player1, List.of(new UnexpectedlyAbsent()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(ownersTopCard, creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllersTopCard);
    }
}
