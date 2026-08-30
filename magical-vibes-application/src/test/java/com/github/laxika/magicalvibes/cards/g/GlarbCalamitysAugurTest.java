package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlarbCalamitysAugur.class, Forest.class, GrizzlyBears.class, HillGiant.class})
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

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(hillGiant);
    }

    @Test
    @DisplayName("Cannot cast a spell with mana value less than 4 from the top of the library")
    void cannotCastLowManaValueSpellFromTopOfLibrary() {
        harness.addToBattlefield(player1, new GlarbCalamitysAugur());
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(grizzlyBears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(grizzlyBears);
    }

    @Test
    @DisplayName("Tapping Glarb surveils two cards")
    void tappingGlarbSurveilsTwo() {
        Permanent glarb = addReadyGlarb();
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

    private Permanent addReadyGlarb() {
        Permanent glarb = harness.addToBattlefieldAndReturn(player1, new GlarbCalamitysAugur());
        glarb.setSummoningSick(false);
        return glarb;
    }
}
