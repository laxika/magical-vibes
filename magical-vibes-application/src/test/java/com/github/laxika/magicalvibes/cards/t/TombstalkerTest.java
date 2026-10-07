package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tombstalker.class, NessianCourser.class})
class TombstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new NessianCourser(), new NessianCourser(), new NessianCourser(),
                new NessianCourser(), new NessianCourser(), new NessianCourser());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Tombstalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Tombstalker);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Tombstalker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.addToBattlefield(player2, new NessianCourser());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Delve reduces only generic mana and leaves unexiled graveyard cards behind")
    void partialDelvePaysOnlyGenericMana() {
        Card exiled = new Tombstalker();
        Card remaining = new Tombstalker();
        harness.setGraveyard(player1, List.of(exiled, remaining));
        harness.setHand(player1, List.of(new Tombstalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Tombstalker);
    }

    @Test
    @DisplayName("Delve is optional even with cards available in the graveyard")
    void canPayFullManaWithoutDelving() {
        Card remaining = new NessianCourser();
        harness.setGraveyard(player1, List.of(remaining));
        harness.setHand(player1, List.of(new Tombstalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Tombstalker);
    }

    @Test
    @DisplayName("Delve cannot replace the black mana requirement")
    void delveCannotPayColoredMana() {
        List<Card> graveyard = List.of(
                new NessianCourser(), new NessianCourser(), new NessianCourser(),
                new NessianCourser(), new NessianCourser(), new NessianCourser());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Tombstalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Delve cannot exile more cards than the generic cost")
    void cannotOverpayWithDelve() {
        List<Card> graveyard = List.of(
                new NessianCourser(), new NessianCourser(), new NessianCourser(),
                new NessianCourser(), new NessianCourser(), new NessianCourser(),
                new NessianCourser());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Tombstalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5, 6)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
