package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlipTheSwitch;
import com.github.laxika.magicalvibes.cards.g.Geistwave;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
import com.github.laxika.magicalvibes.cards.s.SpellCrumple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaithookAngler.class, HookHauntDrifter.class, FlipTheSwitch.class,
        Geistwave.class, SpellCrumple.class})
class BaithookAnglerTest extends BaseCardTest {

    @Test
    @DisplayName("Disturb casts Baithook Angler from the graveyard transformed")
    void disturbEntersTransformed() {
        Permanent drifter = castWithDisturb();

        assertThat(drifter.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hook-Haunt Drifter is exiled instead of going to the graveyard")
    void drifterIsExiledInsteadOfGraveyard() {
        Permanent drifter = castWithDisturb();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, drifter));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(drifter.getOriginalCard().getId());
    }

    @Test
    void frontFaceGoesToGraveyardNormally() {
        Permanent angler = harness.addToBattlefieldAndReturn(player1, new BaithookAngler());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, angler));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(angler.getOriginalCard());
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void counteredDisturbSpellIsExiled() {
        BaithookAngler angler = prepareDisturb();
        harness.setHand(player2, List.of(new FlipTheSwitch()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angler.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(angler.getId());
    }

    @Test
    void bouncedDrifterReturnsToHandAndCanBeCastAsFrontFace() {
        Permanent drifter = castWithDisturb();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Geistwave()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.castAndResolveInstant(player2, 0, drifter.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(drifter.getOriginalCard());
        assertThat(gd.exiledCards).isEmpty();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent angler = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(angler.isTransformed()).isFalse();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, angler));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drifter.getOriginalCard());
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void spellCrumplePutsDisturbedCardOnBottomOfLibraryInsteadOfExilingIt() {
        BaithookAngler angler = prepareDisturb();
        harness.setLibrary(player1, List.of(new BaithookAngler()));
        harness.setHand(player2, List.of(new SpellCrumple()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angler.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(angler.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void disturbedDrifterCannotBeBlockedByGroundCreature() {
        Permanent drifter = castWithDisturb();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BaithookAngler());

        assertThat(bls.canBlockAttacker(gd, blocker, drifter,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void disturbRequiresBlueMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        BaithookAngler angler = new BaithookAngler();
        harness.setGraveyard(player1, List.of(angler));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(angler);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private BaithookAngler prepareDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        BaithookAngler angler = new BaithookAngler();
        harness.setGraveyard(player1, List.of(angler));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        return angler;
    }

    private Permanent castWithDisturb() {
        prepareDisturb();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.castAndResolveFlashback(player1, 0, null));
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
