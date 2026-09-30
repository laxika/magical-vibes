package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CorvidSquall;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, galathul.getPreparedSpellCardId());
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Storm Crow")).hasSize(2);
    }

    private void prepareGalathul() {
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isPrepared()).isTrue();
    }
}
