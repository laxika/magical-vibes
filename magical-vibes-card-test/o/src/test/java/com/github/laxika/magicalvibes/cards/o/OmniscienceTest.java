package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.cards.w.WildGuess;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Omniscience.class, TimberpackWolf.class, Divination.class, VolcanicGeyser.class, WildGuess.class})
class OmniscienceTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spell can be cast from hand with no mana available")
    void creatureCastForFree() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new TimberpackWolf()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Timberpack Wolf");
    }

    @Test
    @DisplayName("Noncreature spell can also be cast from hand for free")
    void sorceryCastForFree() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new Divination()));

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Free cast spends no mana from the pool")
    void freeCastSpendsNoMana() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new TimberpackWolf()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's spells are not made free by your Omniscience")
    void opponentSpellsNotFree() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player2, List.of(new TimberpackWolf()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void freeXSpellDealsZeroDamage() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Volcanic Geyser");
    }

    @Test
    void positiveXCannotBeCastWithoutMana() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new VolcanicGeyser()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Volcanic Geyser");
    }

    @Test
    void positiveXCanBeCastByPayingNormalManaCost() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void instantCanBeCastForFreeOnOpponentsTurn() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.forceActivePlayer(player2);

        harness.castInstant(player1, 0, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void sorceryStillRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new Divination()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantPermissionToCastFromGraveyard() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setGraveyard(player1, List.of(new TimberpackWolf()));

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Timberpack Wolf");
    }

    @Test
    void mandatoryDiscardIsStillRequired() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new WildGuess(), new TimberpackWolf()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mandatoryDiscardCanBePaidWithoutPayingManaCost() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new WildGuess(), new TimberpackWolf()));
        harness.setLibrary(player1, List.of(new Divination(), new VolcanicGeyser()));

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Timberpack Wolf");
        harness.assertInGraveyard(player1, "Wild Guess");
        harness.assertInHand(player1, "Divination");
        harness.assertInHand(player1, "Volcanic Geyser");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Permission is gone once Omniscience leaves the battlefield")
    void permissionEndsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new TimberpackWolf()));

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Omniscience"));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
