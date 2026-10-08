package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Waterwhirl.class, GrizzlyBears.class, Island.class, WetlandSambar.class})
class WaterwhirlTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Waterwhirl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(mine.getId(), theirs.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a single target creature")
    void returnsOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Waterwhirl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Waterwhirl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can resolve with zero targets while creatures are present")
    void resolvesWithZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new Waterwhirl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Waterwhirl");
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void cannotTargetThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new Waterwhirl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns the remaining target if the other leaves the battlefield")
    void returnsRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new Waterwhirl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first.getCard());
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner rather than its controller")
    void returnsStolenCreatureToOwner() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new WetlandSambar());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.setHand(player1, List.of(new Waterwhirl()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, stolen.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolen.getCard());
    }
}
