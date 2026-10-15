package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.p.PutridRaptor;
import com.github.laxika.magicalvibes.cards.z.ZombieCutthroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExiledDoomsayer.class, ScornfulEgotist.class, PutridRaptor.class, ZombieCutthroat.class, DogWalker.class})
class ExiledDoomsayerTest extends BaseCardTest {
    @Test
    void doesNotIncreaseDisguiseCost() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent walker = findPermanent(player1, "Dog Walker");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(walker));
        resolveAllTriggers();

        assertThat(walker.isFaceDown()).isFalse();
        assertThat(countPermanents(player1, "Dog")).isEqualTo(2);
    }
    @Test
    void multipleDoomsayersAddTheirSurcharges() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.addToBattlefield(player2, new ExiledDoomsayer());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(egotist);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, index))
                .isInstanceOf(IllegalStateException.class);
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, index);
        assertThat(egotist.isFaceDown()).isFalse();
    }

    @Test
    void surchargeEndsWhenDoomsayerLeavesBattlefield() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        Permanent doomsayer = findPermanent(player1, "Exiled Doomsayer");
        gd.playerBattlefields.get(player1.getId()).remove(doomsayer);
        gd.playerGraveyards.get(player1.getId()).add(doomsayer.getCard());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist));
        assertThat(egotist.isFaceDown()).isFalse();
    }

    @Test
    void discardMorphCostAlsoRequiresTwoMana() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        ZombieCutthroat zombie = new ZombieCutthroat();
        harness.setHand(player1, List.of(new PutridRaptor(), zombie));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent raptor = findPermanent(player1, "Putrid Raptor");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(raptor);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, index, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(raptor.isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(zombie);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, index, 0);
        assertThat(raptor.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(zombie);
    }

    @Test
    void lifeMorphCostAlsoRequiresTwoMana() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.setHand(player1, List.of(new ZombieCutthroat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent zombie = findPermanent(player1, "Zombie Cutthroat");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);
        int life = gd.playerLifeTotals.get(player1.getId());
        assertThatThrownBy(() -> harness.turnFaceUp(player1, index))
                .isInstanceOf(IllegalStateException.class);
        assertThat(zombie.isFaceDown()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, index);
        assertThat(zombie.isFaceDown()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(life - 5);
    }

    @Test
    void increasesMorphCostWithoutIncreasingFaceDownCastCost() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.setHand(player1, List.of(new ScornfulEgotist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent egotist = findPermanent(player1, "Scornful Egotist");
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist)))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(egotist));

        assertThat(egotist.isFaceDown()).isFalse();
    }

    @Test
    void increasesOpponentsMorphCost() {
        harness.addToBattlefield(player1, new ExiledDoomsayer());
        harness.setHand(player2, List.of(new ScornfulEgotist()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.castCreatureWithMorph(player2, 0);
        resolveAllTriggers();

        Permanent egotist = findPermanent(player2, "Scornful Egotist");
        assertThat(egotist.isFaceDown()).isTrue();

        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.turnFaceUp(
                player2, gd.playerBattlefields.get(player2.getId()).indexOf(egotist)))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(egotist));

        assertThat(egotist.isFaceDown()).isFalse();
    }
}
