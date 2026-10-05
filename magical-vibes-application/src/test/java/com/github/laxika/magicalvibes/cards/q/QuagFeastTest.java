package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ChandraSparkHunter;
import com.github.laxika.magicalvibes.cards.i.InterfaceAce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuagFeast.class, AirResponseUnit.class, ChandraSparkHunter.class, InterfaceAce.class, Forest.class})
class QuagFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards before destroying a creature within the graveyard threshold")
    void millsBeforeCheckingThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InterfaceAce());

        cast(List.of(), target.getId());

        harness.assertNotOnBattlefield(player2, "Interface Ace");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not destroy a target whose mana value is above the caster's graveyard size")
    void doesNothingAboveGraveyardThreshold() {
        Permanent planeswalker = addReadyPlaneswalker(player2);

        cast(List.of(), planeswalker.getId());

        harness.assertOnBattlefield(player2, "Chandra, Spark Hunter");
    }

    @Test
    @DisplayName("Can destroy a Vehicle")
    void destroysVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());

        cast(List.of(new Forest(), new Forest(), new Forest(), new Forest()), vehicle.getId());

        harness.assertNotOnBattlefield(player2, "Air Response Unit");
    }

    @Test
    @DisplayName("Cannot target a permanent that is not a creature, planeswalker, or Vehicle")
    void rejectsOtherPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new QuagFeast()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a planeswalker at the exact graveyard threshold")
    void destroysPlaneswalkerAtThreshold() {
        Permanent planeswalker = addReadyPlaneswalker(player2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        cast(List.of(new Forest(), new Forest()), planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Chandra, Spark Hunter");
        harness.assertInGraveyard(player2, "Chandra, Spark Hunter");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Quag Feast itself does not count toward the destruction threshold")
    void resolvingSpellDoesNotCountTowardThreshold() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        cast(List.of(), vehicle.getId());

        harness.assertOnBattlefield(player2, "Air Response Unit");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills only the remaining card when the library contains fewer than two")
    void shortLibraryDoesNotReachThreshold() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InterfaceAce());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(List.of(), creature.getId());

        harness.assertOnBattlefield(player2, "Interface Ace");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can destroy its controller's creature and mills only its controller")
    void canTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new InterfaceAce());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Forest opponentLibraryCard = new Forest();
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.setGraveyard(player2, List.of());

        cast(List.of(), creature.getId());

        harness.assertNotOnBattlefield(player1, "Interface Ace");
        harness.assertInGraveyard(player1, "Interface Ace");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not mill when its only target has left the battlefield")
    void illegalTargetPreventsMilling() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InterfaceAce());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new QuagFeast()));
        addCastingMana();
        harness.castSorcery(player1, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Quag Feast");
    }

    private void cast(List<Card> graveyard, UUID targetId) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new QuagFeast()));
        addCastingMana();

        harness.castAndResolveSorcery(player1, 0, List.of(targetId));
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraSparkHunter());
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        return permanent;
    }
}
