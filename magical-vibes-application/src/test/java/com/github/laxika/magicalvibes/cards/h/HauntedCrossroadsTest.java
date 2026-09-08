package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HauntedCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target creature card from the graveyard on top of the library")
    void putsTargetCreatureOnTopOfLibrary() {
        int crossroadsIndex = addCrossroadsIndex();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HolyDay())));

        harness.activateAbilityWithGraveyardTargets(player1, crossroadsIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are legal targets")
    void rejectsInvalidGraveyardTargets() {
        int crossroadsIndex = addCrossroadsIndex();
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card nonCreature = new HolyDay();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(nonCreature)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCreature)));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, crossroadsIndex, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, crossroadsIndex, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int addCrossroadsIndex() {
        Permanent crossroads = harness.addToBattlefieldAndReturn(player1, new HauntedCrossroads());
        return gd.playerBattlefields.get(player1.getId()).indexOf(crossroads);
    }
}
