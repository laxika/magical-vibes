package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.d.DefyGravity;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShamansTrance.class, MentalNote.class, KrosanVerge.class, DefyGravity.class,
        BenevolentBodyguard.class})
class ShamansTranceTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a spell from an opponent's graveyard and returns it to its owner's graveyard")
    void castsSpellFromOpponentsGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        MentalNote mentalNote = new MentalNote();
        harness.setHand(player1, List.of(trance));
        harness.setGraveyard(player2, List.of(mentalNote));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromGraveyard(player1, mentalNote.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(mentalNote);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(trance);
    }

    @Test
    @DisplayName("Allows playing a land from an opponent's graveyard")
    void playsLandFromOpponentsGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setHand(player1, List.of(trance));
        harness.setGraveyard(player2, List.of(krosanVerge));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, krosanVerge.getId());

        harness.assertOnBattlefield(player1, "Krosan Verge");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not prevent its controller from casting from their own graveyard")
    void controllerCanStillCastFromOwnGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        DefyGravity defyGravity = new DefyGravity();
        Permanent target = addCreatureReady(player1, new BenevolentBodyguard());
        harness.setHand(player1, List.of(trance));
        harness.setGraveyard(player1, List.of(defyGravity));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromGraveyardTargeting(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(defyGravity);
    }

    @Test
    @DisplayName("Prevents other players from playing cards from their graveyards")
    void preventsOtherPlayersFromPlayingFromTheirGraveyards() {
        ShamansTrance trance = new ShamansTrance();
        MentalNote mentalNote = new MentalNote();
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setHand(player1, List.of(trance));
        harness.setGraveyard(player2, List.of(mentalNote, krosanVerge));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        prepareMainPhase(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, mentalNote.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.playGraveyardLand(player2, krosanVerge.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
