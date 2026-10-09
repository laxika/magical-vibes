package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.cards.c.CinderhazeWretch;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
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

@CardUsed({DisturbingPlot.class, Cinderbones.class, CinderhazeWretch.class, SafeholdSentry.class})
class DisturbingPlotTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from graveyard to hand")
    void returnsTargetCreatureToHand() {
        Card creature = new Cinderbones();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can target an opponent's graveyard; the card returns to its owner's hand")
    void returnsFromOpponentGraveyardToOwnersHand() {
        Card creature = new Cinderbones();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // "to its owner's hand" — the opponent's card goes to the opponent's hand, not the caster's.
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreature() {
        Card nonCreature = new DisturbingPlot();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire copies the spell and allows choosing a new graveyard target")
    void conspireCopiesSpellAndAllowsNewTarget() {
        Card originalTarget = new Cinderbones();
        Card copyTarget = new CinderhazeWretch();
        harness.setGraveyard(player1, List.of(originalTarget, copyTarget));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent blackCreature1 = addCreatureReady(player1, new Cinderbones());
        Permanent blackCreature2 = addCreatureReady(player1, new CinderhazeWretch());

        harness.castWithConspire(player1, 0, originalTarget.getId(),
                List.of(blackCreature1.getId(), blackCreature2.getId()));

        assertThat(blackCreature1.isTapped()).isTrue();
        assertThat(blackCreature2.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(originalTarget.getId()))
                .anyMatch(card -> card.getId().equals(copyTarget.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(originalTarget.getId()))
                .noneMatch(card -> card.getId().equals(copyTarget.getId()));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsNonBlackCreature() {
        Card creature = new Cinderbones();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent blackCreature = addCreatureReady(player1, new Cinderbones());
        Permanent whiteCreature = addCreatureReady(player1, new SafeholdSentry());

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, creature.getId(),
                List.of(blackCreature.getId(), whiteCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning-sick creatures can conspire and the copy may keep its original target")
    void conspireWithSummoningSickCreaturesKeepsTarget() {
        Card target = new Cinderbones();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Cinderbones());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CinderhazeWretch());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.castWithConspire(player1, 0, target.getId(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getId().equals(target.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof DisturbingPlot);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Conspire can retarget its copy to a creature in the opponent's graveyard")
    void conspireCopyReturnsOpponentCardToOwner() {
        harness.setHand(player2, List.of());
        Card originalTarget = new Cinderbones();
        Card copyTarget = new CinderhazeWretch();
        harness.setGraveyard(player1, List.of(originalTarget));
        harness.setGraveyard(player2, List.of(copyTarget));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        Permanent first = addCreatureReady(player1, new Cinderbones());
        Permanent second = addCreatureReady(player1, new CinderhazeWretch());

        harness.castWithConspire(player1, 0, originalTarget.getId(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalTarget);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(copyTarget);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution is not returned")
    void removedTargetIsNotReturned() {
        Card target = new Cinderbones();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Conspire cannot use an already tapped creature")
    void conspireRejectsTappedCreature() {
        Card target = new Cinderbones();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DisturbingPlot()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        Permanent first = addCreatureReady(player1, new Cinderbones());
        Permanent second = addCreatureReady(player1, new CinderhazeWretch());
        second.tap();

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, target.getId(),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
    }
}
