package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MesaLynx;
import com.github.laxika.magicalvibes.cards.s.SpareSupplies;
import com.github.laxika.magicalvibes.cards.f.FearlessFledgling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodBeckoning.class, MesaLynx.class, FearlessFledgling.class, SpareSupplies.class})
class BloodBeckoningTest extends BaseCardTest {

    @Test
    void returnsOneTargetWithoutKicker() {
        Card creature = new MesaLynx();
        Card otherCreature = new FearlessFledgling();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(otherCreature.getId()).doesNotContain(creature.getId());
        harness.assertInGraveyard(player1, "Blood Beckoning");
    }

    @Test
    void returnsTwoTargetsWhenKicked() {
        Card creature = new MesaLynx();
        Card otherCreature = new FearlessFledgling();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), otherCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), otherCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Blood Beckoning");
    }

    @Test
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new MesaLynx();
        Card artifact = new SpareSupplies();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void kickedCastRequiresTwoCreatureCards() {
        harness.setGraveyard(player1, List.of(new MesaLynx()));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithOnlyAnOpponentsCreatureInAGraveyard() {
        harness.setGraveyard(player2, List.of(new MesaLynx()));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetChoiceExcludesOpponentsGraveyard() {
        Card creature = new MesaLynx();
        Card opponentsCreature = new FearlessFledgling();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void kickedSpellReturnsRemainingLegalTarget() {
        Card creature = new MesaLynx();
        Card removedCreature = new FearlessFledgling();
        harness.setGraveyard(player1, List.of(creature, removedCreature));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), removedCreature.getId()));
        harness.setGraveyard(player1, List.of(creature));
        harness.setExile(player1, List.of(removedCreature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(removedCreature);
        harness.assertInGraveyard(player1, "Blood Beckoning");
    }

    @Test
    void spellDoesNotReturnAnotherCardWhenItsTargetLeavesTheGraveyard() {
        Card target = new MesaLynx();
        Card otherCreature = new FearlessFledgling();
        harness.setGraveyard(player1, List.of(target, otherCreature));
        harness.setHand(player1, List.of(new BloodBeckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(target);
        harness.assertInGraveyard(player1, "Fearless Fledgling");
        harness.assertInGraveyard(player1, "Blood Beckoning");
    }
}
