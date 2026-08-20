package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SilverquillCommandTest extends BaseCardTest {

    @Test
    void boostsCreatureAndMakesPlayerDrawAndLoseLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card draw = new Forest();
        harness.setLibrary(player2, List.of(draw));
        harness.setHand(player2, List.of());
        prepareSpell();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInHand(player2, draw.getName());
    }

    @Test
    void returnsSmallCreatureAndOpponentSacrificesTheirChoice() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        prepareSpell();

        castWithModes(new int[]{1, 3}, graveyardCreature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, sacrificed.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void allowsTheSameOpponentForDrawAndSacrificeModes() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card draw = new Forest();
        harness.setLibrary(player2, List.of(draw));
        harness.setHand(player2, List.of());
        prepareSpell();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, sacrificed.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInHand(player2, draw.getName());
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureBoostModeRejectsNonCreatureTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2,
                new int[]{0, 2}, List.of(forest.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithModes(int[] modes, UUID targetId, List<UUID> targetIds) {
        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(2, modes),
                targetId, null, targetIds, List.of());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SilverquillCommand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
