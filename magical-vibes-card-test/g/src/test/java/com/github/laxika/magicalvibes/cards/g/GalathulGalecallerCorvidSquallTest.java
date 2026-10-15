package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CorvidSquall;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
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

@CardUsed({GalathulGalecallerCorvidSquall.class, CorvidSquall.class, StormCrow.class})
class GalathulGalecallerCorvidSquallTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever Galathul Galecaller attacks, it becomes prepared")
    void attackPreparesGalathul() {
        Permanent galathul = addCreatureReady(player1, new GalathulGalecallerCorvidSquall());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(galathul.isPrepared()).isTrue();
        assertThat(galathul.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(galathul.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Casting Corvid Squall unprepares Galathul and resolves its Storm Crow conjure")
    void castingPreparedSpellUnpreparesAndConjuresStormCrow() {
        Permanent galathul = addCreatureReady(player1, new GalathulGalecallerCorvidSquall());
        prepareGalathul();
        UUID preparedSpellId = galathul.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, preparedSpellId);
        resolveAllTriggers();

        assertThat(galathul.isPrepared()).isFalse();
        assertThat(galathul.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(preparedSpellId)).isNull();
        assertThat(findPermanents(player1, "Storm Crow")).hasSize(1);
    }

    @Test
    @DisplayName("Corvid Squall's Storm copies the Storm Crow conjure for each prior spell")
    void stormCopiesConjure() {
        gd.recordSpellCast(player1.getId(), new StormCrow());
        Permanent galathul = addCreatureReady(player1, new GalathulGalecallerCorvidSquall());
        prepareGalathul();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, galathul.getPreparedSpellCardId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Storm Crow")).hasSize(2);
    }

    @Test
    @DisplayName("Attacking while already prepared retains the same prepare spell")
    void attackingWhilePreparedDoesNotCreateAnotherSpell() {
        Permanent galathul = addCreatureReady(player1, new GalathulGalecallerCorvidSquall());
        prepareGalathul();
        UUID preparedSpellId = galathul.getPreparedSpellCardId();
        int exiledCards = gd.exiledCards.size();

        harness.performUntapStep(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(galathul.isPrepared()).isTrue();
        assertThat(galathul.getPreparedSpellCardId()).isEqualTo(preparedSpellId);
        assertThat(gd.exiledCards).hasSize(exiledCards);
    }

    @Test
    @DisplayName("Casting one Galathul's prepare spell leaves the other Galathul prepared")
    void castingOnlyUnpreparesLinkedPermanent() {
        Permanent first = addCreatureReady(player1, new GalathulGalecallerCorvidSquall());
        Permanent second = addCreatureReady(player1, new GalathulGalecallerCorvidSquall());
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        UUID secondSpellId = second.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, first.getPreparedSpellCardId());
        resolveAllTriggers();

        assertThat(first.isPrepared()).isFalse();
        assertThat(second.isPrepared()).isTrue();
        assertThat(second.getPreparedSpellCardId()).isEqualTo(secondSpellId);
        assertThat(gd.findExiledCard(secondSpellId)).isNotNull();
        assertThat(findPermanents(player1, "Storm Crow")).hasSize(1);
    }

    @Test
    @DisplayName("Storm counts an opponent's earlier spell, including a flashed Galathul")
    void stormCountsOpponentsSpell() {
        addCreatureReady(player1, new GalathulGalecallerCorvidSquall());
        prepareGalathul();
        UUID preparedSpellId = gd.playerBattlefields.get(player1.getId()).getFirst().getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new GalathulGalecallerCorvidSquall()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player2, "Galathul Galecaller")).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, preparedSpellId);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Storm Crow")).hasSize(2);
        assertThat(findPermanents(player2, "Storm Crow")).isEmpty();
    }

    @Test
    @DisplayName("Corvid Squall does not inherit Galathul's flash")
    void preparedSorceryCannotBeCastDuringUpkeep() {
        Permanent galathul = addCreatureReady(player1, new GalathulGalecallerCorvidSquall());
        prepareGalathul();
        UUID preparedSpellId = galathul.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, preparedSpellId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(galathul.isPrepared()).isTrue();
        assertThat(galathul.getPreparedSpellCardId()).isEqualTo(preparedSpellId);
        assertThat(gd.findExiledCard(preparedSpellId)).isNotNull();
        assertThat(findPermanents(player1, "Storm Crow")).isEmpty();
    }

    private void prepareGalathul() {
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isPrepared()).isTrue();
    }
}
