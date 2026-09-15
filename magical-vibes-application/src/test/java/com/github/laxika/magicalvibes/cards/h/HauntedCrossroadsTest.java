package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.r.RishadanCutpurse;
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

@CardUsed({HauntedCrossroads.class, RishadanCutpurse.class, Brainstorm.class})
class HauntedCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target creature card from the graveyard on top of the library")
    void putsTargetCreatureOnTopOfLibrary() {
        int crossroadsIndex = addCrossroadsIndex();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card creature = new RishadanCutpurse();
        Card existingTop = new Brainstorm();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(existingTop));

        harness.activateAbilityWithGraveyardTargets(player1, crossroadsIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId(), existingTop.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are legal targets")
    void rejectsInvalidGraveyardTargets() {
        int crossroadsIndex = addCrossroadsIndex();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card nonCreature = new Brainstorm();
        Card opponentCreature = new RishadanCutpurse();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, crossroadsIndex, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, crossroadsIndex, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires a creature card target in your graveyard")
    void requiresCreatureTarget() {
        int crossroadsIndex = addCrossroadsIndex();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, crossroadsIndex, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the targeted creature card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        int crossroadsIndex = addCrossroadsIndex();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card creature = new RishadanCutpurse();
        Card existingTop = new Brainstorm();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(existingTop));

        harness.activateAbilityWithGraveyardTargets(player1, crossroadsIndex, 0, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(existingTop.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private int addCrossroadsIndex() {
        Permanent crossroads = harness.addToBattlefieldAndReturn(player1, new HauntedCrossroads());
        return gd.playerBattlefields.get(player1.getId()).indexOf(crossroads);
    }
}
