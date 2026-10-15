package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GenerousSoul;
import com.github.laxika.magicalvibes.cards.g.Geistwave;
import com.github.laxika.magicalvibes.cards.f.FlipTheSwitch;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelovedBeggar.class, GenerousSoul.class, FlipTheSwitch.class, InfernalGrasp.class, Geistwave.class})
class BelovedBeggarTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts Beloved Beggar from the graveyard transformed as Generous Soul")
    void disturbEntersTransformed() {
        Permanent soul = castWithDisturb();

        assertThat(soul.isTransformed()).isTrue();
        assertThat(soul.getCard().getName()).isEqualTo("Generous Soul");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Generous Soul is exiled instead of going to the graveyard")
    void generousSoulExiledInsteadOfGraveyard() {
        Permanent soul = castWithDisturb();
        UUID soulId = soul.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, soul));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(soulId);
    }

    private Permanent castWithDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BelovedBeggar()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveFlashback(player1, 0, null);
        return findPermanent(player1, "Generous Soul");
    }

    @Test
    void frontFaceGoesToGraveyardNormally() {
        harness.castFromHand(player1, new BelovedBeggar(), "{1}{W}");
        harness.passBothPriorities();
        Permanent beggar = findPermanent(player1, "Beloved Beggar");
        UUID cardId = beggar.getOriginalCard().getId();

        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, beggar.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getId()).contains(cardId);
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).doesNotContain(cardId);
        harness.assertNotOnBattlefield(player1, "Beloved Beggar");
    }

    @Test
    void destroyedBackFaceIsExiled() {
        Permanent soul = castWithDisturb();
        UUID cardId = soul.getOriginalCard().getId();
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, soul.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    void counteredDisturbSpellIsExiled() {
        BelovedBeggar beggar = new BelovedBeggar();
        harness.setGraveyard(player1, List.of(beggar));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castFlashback(player1, 0);
        harness.setHand(player2, List.of(new FlipTheSwitch()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, beggar.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(beggar.getId());
    }

    @Test
    void disturbRequiresSixManaIncludingTwoWhite() {
        harness.setGraveyard(player1, List.of(new BelovedBeggar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Beloved Beggar");
    }

    @Test
    void disturbCannotBeCastDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new BelovedBeggar()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Beloved Beggar");
    }

    @Test
    void disturbCannotBePaidWithOnlyFiveMana() {
        harness.setGraveyard(player1, List.of(new BelovedBeggar()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Beloved Beggar");
    }

    @Test
    void bouncedSoulReturnsAsBeggarAndCanBeCastNormally() {
        Permanent soul = castWithDisturb();
        UUID cardId = soul.getOriginalCard().getId();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Geistwave()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, soul.getId());

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId()).containsExactly(cardId);
        harness.assertInHand(player1, "Beloved Beggar");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).doesNotContain(cardId);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent beggar = findPermanent(player1, "Beloved Beggar");
        assertThat(beggar.isTransformed()).isFalse();
        assertThat(beggar.getOriginalCard().getId()).isEqualTo(cardId);
    }
}
