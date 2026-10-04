package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Stargaze;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlarbCalamitysAugur.class, Forest.class, GrizzlyBears.class, HillGiant.class, Stargaze.class})
class GlarbCalamitysAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a land from the top of the library")
    void canPlayLandFromTopOfLibrary() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can cast a spell with mana value 4 or greater from the top of the library")
    void canCastHighManaValueSpellFromTopOfLibrary() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card hillGiant = new HillGiant();
        harness.setLibrary(player1, List.of(hillGiant));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(hillGiant);
    }

    @Test
    @DisplayName("Cannot cast a spell with mana value less than 4 from the top of the library")
    void cannotCastLowManaValueSpellFromTopOfLibrary() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(grizzlyBears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(grizzlyBears);
    }

    @Test
    @DisplayName("Tapping Glarb surveils two cards")
    void tappingGlarbSurveilsTwo() {
        Permanent glarb = addCreatureReady(player1, new GlarbCalamitysAugur());
        Card topCard = new Forest();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glarb.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    void canCastXSpellWhenChosenXMakesManaValueFour() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card stargaze = new Stargaze();
        harness.setLibrary(player1, List.of(stargaze));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        gs.playCardFromLibraryTop(gd, player1, 2, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(stargaze);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastXSpellWhenChosenXLeavesManaValueBelowFour() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card stargaze = new Stargaze();
        harness.setLibrary(player1, List.of(stargaze));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardFromLibraryTop(gd, player1, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(stargaze);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void topCardIsVisibleOnlyToControllerEvenWithoutPriority() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        harness.setLibrary(player1, List.of(new Stargaze()));
        harness.ensurePriority(player2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Stargaze"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Stargaze"));
    }

    @Test
    void landFromLibraryStillUsesNormalLandAllowance() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    void creatureFromLibraryStillRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card giant = new HillGiant();
        harness.setLibrary(player1, List.of(giant));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(giant);
    }

    @Test
    void cannotActivateTapAbilityWhileSummoningSick() {
        Permanent glarb = harness.addToBattlefieldAndReturn(player1, new GlarbCalamitysAugur());
        glarb.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(glarb.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void surveilCanKeepBothCardsInEitherOrder() {
        addCreatureReady(player1, new GlarbCalamitysAugur());
        Card first = new Forest();
        Card second = new Stargaze();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsInGraveyard() {
        addCreatureReady(player1, new GlarbCalamitysAugur());
        Card first = new Forest();
        Card second = new Stargaze();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void surveilWorksWithOnlyOneCardInLibrary() {
        addCreatureReady(player1, new GlarbCalamitysAugur());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void highManaValueSpellStillRequiresItsManaCost() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card giant = new HillGiant();
        harness.setLibrary(player1, List.of(giant));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(giant);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void surveilWithEmptyLibraryStillTapsGlarb() {
        Permanent glarb = addCreatureReady(player1, new GlarbCalamitysAugur());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glarb.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
