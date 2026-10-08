package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgRepossession.class, GrizzlyBears.class, LeoninScimitar.class, Shock.class})
class UrborgRepossessionTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureAndGainsLife() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        harness.setLife(player1, 10);
        addBaseMana();

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(artifact.getId());
    }

    @Test
    void kickedCastReturnsAnotherTargetPermanentAndGainsLife() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        harness.setLife(player1, 10);
        addKickedMana();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), artifact.getId()), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), artifact.getId());
        harness.assertInGraveyard(player1, "Urborg Repossession");
    }

    @Test
    void kickedCastRequiresAnotherPermanentTarget() {
        Card creature = new GrizzlyBears();
        Card nonPermanent = new Shock();
        harness.setGraveyard(player1, List.of(creature, nonPermanent));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        addKickedMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), nonPermanent.getId()), List.of(), false,
                null, null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedCastCannotUseTheSameCardForBothTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        addKickedMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), creature.getId()), List.of(), false,
                null, null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedCastCanReturnTwoDifferentCreatures() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        addKickedMana();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(first.getId(), second.getId()), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.assertLife(player1, 22);
    }

    @Test
    void cannotTargetAnOpponentsCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseAnArtifactAsTheCreatureTarget() {
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedCastRequiresTheSecondTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        addKickedMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId()), List.of(), false,
                null, null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGainLifeWhenTheOnlyTargetLeavesTheGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        harness.setLife(player1, 10);
        addBaseMana();

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Urborg Repossession");
    }

    @Test
    void kickedCastReturnsThePermanentAndGainsLifeWhenTheCreatureTargetLeaves() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        harness.setLife(player1, 10);
        addKickedMana();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), artifact.getId()), List.of(), false,
                null, null, null, null, null, true);
        harness.setGraveyard(player1, List.of(artifact));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(artifact.getId());
    }

    @Test
    void kickedCastReturnsTheCreatureAndGainsLifeWhenThePermanentTargetLeaves() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        harness.setLife(player1, 10);
        addKickedMana();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), artifact.getId()), List.of(), false,
                null, null, null, null, null, true);
        harness.setGraveyard(player1, List.of(creature));
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId());
    }

    @Test
    void kickedCastDoesNotGainLifeWhenBothTargetsLeave() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new UrborgRepossession()));
        harness.setLife(player1, 10);
        addKickedMana();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), artifact.getId()), List.of(), false,
                null, null, null, null, null, true);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature, artifact));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Urborg Repossession");
    }
    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
