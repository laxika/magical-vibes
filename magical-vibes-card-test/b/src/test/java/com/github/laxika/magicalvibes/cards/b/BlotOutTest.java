package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlotOut.class, GarrukWildspeaker.class, GrizzlyBears.class, HillGiant.class})
class BlotOutTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the greatest-mana-value permanent across the opponent's creatures and planeswalkers")
    void exilesGreatestCreatureOrPlaneswalker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = addReadyPlaneswalker(player2);

        castAtOpponent();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(planeswalker.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Garruk Wildspeaker"));
    }

    @Test
    @DisplayName("Determines the permanent to exile as the spell resolves")
    void determinesGreatestPermanentAtResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAtOpponent();
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bears.getId()))
                .noneMatch(permanent -> permanent.getId().equals(hillGiant.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Lets the targeted opponent choose among tied greatest permanents")
    void targetedOpponentChoosesAmongTies() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAtOpponent();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new BlotOut()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castAtOpponent() {
        harness.setHand(player1, List.of(new BlotOut()));
        addMana();
        harness.castInstant(player1, 0, player2.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void resolvesWithoutEligiblePermanents() {
        harness.addToBattlefield(player1, new HillGiant());

        castAtOpponent();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCanChoosePlaneswalkerTiedWithCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent planeswalker = addReadyPlaneswalker(player2);

        castAtOpponent();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), planeswalker.getId());
        harness.handlePermanentChosen(player2, planeswalker.getId());

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Garruk Wildspeaker"));
    }

    @Test
    void faceDownCreatureHasZeroManaValue() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAtOpponent();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(faceDown.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
