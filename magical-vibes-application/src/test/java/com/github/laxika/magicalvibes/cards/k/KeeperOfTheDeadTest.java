package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Deathlace;
import com.github.laxika.magicalvibes.cards.f.Fugue;
import com.github.laxika.magicalvibes.cards.m.MemoryCrystal;
import com.github.laxika.magicalvibes.cards.p.PlatedRootwalla;
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

@CardUsed({KeeperOfTheDead.class, PlatedRootwalla.class, Fugue.class, MemoryCrystal.class})
class KeeperOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target nonblack creature controlled by the targeted opponent")
    void destroysTargetNonblackCreature() {
        readyKeeper(List.of(new PlatedRootwalla(), new PlatedRootwalla(), new Fugue()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedRootwalla());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(target.getCard().getId());
    }

    @Test
    @DisplayName("Checks the graveyard difference only when activating")
    void graveyardDifferenceIsCheckedOnlyOnActivation() {
        readyKeeper(List.of(new PlatedRootwalla(), new PlatedRootwalla()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedRootwalla());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        gd.playerGraveyards.get(player2.getId()).add(new PlatedRootwalla());
        gd.playerGraveyards.get(player2.getId()).add(new PlatedRootwalla());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(target.getId());
    }

    @Test
    @DisplayName("Cannot activate when the targeted opponent has not fallen behind by two creature cards")
    void cannotActivateWithoutRequiredGraveyardDifference() {
        readyKeeper(List.of(new PlatedRootwalla()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedRootwalla());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        readyKeeper(List.of(new PlatedRootwalla(), new PlatedRootwalla()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KeeperOfTheDead());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counts only creature cards for the graveyard difference")
    void countsOnlyCreatureCardsInGraveyards() {
        readyKeeper(List.of(new PlatedRootwalla(), new Fugue(), new Fugue()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedRootwalla());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        readyKeeper(List.of(new PlatedRootwalla(), new PlatedRootwalla()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MemoryCrystal());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by another player")
    void cannotTargetCreatureControlledByAnotherPlayer() {
        readyKeeper(List.of(new PlatedRootwalla(), new PlatedRootwalla()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PlatedRootwalla());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(Deathlace.class)
    @DisplayName("Does not destroy a target that becomes black before resolution")
    void doesNotDestroyTargetThatBecomesBlackBeforeResolution() {
        readyKeeper(List.of(new PlatedRootwalla(), new PlatedRootwalla()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlatedRootwalla());
        harness.setHand(player2, List.of(new Deathlace()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(target.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .doesNotContain(target.getCard().getId());
    }

    private void readyKeeper(List<Card> controllerGraveyard, List<Card> opponentGraveyard) {
        harness.setGraveyard(player1, controllerGraveyard);
        harness.setGraveyard(player2, opponentGraveyard);
        addCreatureReady(player1, new KeeperOfTheDead());
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
