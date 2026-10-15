package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.j.JeskaiDevotee;
import com.github.laxika.magicalvibes.cards.s.SpectralDenial;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnendingWhisper.class, JeskaiDevotee.class, SpectralDenial.class})
class UnendingWhisperTest extends BaseCardTest {

    @Test
    void normalCastDrawsACard() {
        Card drawnCard = new JeskaiDevotee();
        Card spell = new UnendingWhisper();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, spell, "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void harmonizeCastsFromGraveyardAndExilesTheSpell() {
        Card drawnCard = new JeskaiDevotee();
        Card spell = new UnendingWhisper();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashbackWithTapCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void harmonizeReducesGenericCostByTappedCreaturePower() {
        Card drawnCard = new JeskaiDevotee();
        Card spell = new UnendingWhisper();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeCanTapASummoningSickCreature() {
        Card drawnCard = new JeskaiDevotee();
        Card spell = new UnendingWhisper();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        creature.setSummoningSick(true);
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void harmonizeCannotTapAnAlreadyTappedCreature() {
        Card spell = new UnendingWhisper();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        creature.tap();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeCannotTapAnOpponentsCreature() {
        Card spell = new UnendingWhisper();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JeskaiDevotee());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeCannotTapMultipleCreatures() {
        Card spell = new UnendingWhisper();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeReductionDoesNotPayTheBlueManaRequirement() {
        Card spell = new UnendingWhisper();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeWithoutACreatureRequiresItsFullAlternativeCost() {
        Card spell = new UnendingWhisper();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizeDoesNotAllowCastingOnAnOpponentsTurn() {
        Card spell = new UnendingWhisper();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void harmonizedSpellIsExiledWhenCounteredWithoutDrawing() {
        Card drawnCard = new JeskaiDevotee();
        Card spell = new UnendingWhisper();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new SpectralDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFlashbackWithTapCost(player1, 0, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
