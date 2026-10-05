package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AquaticSubtlety;
import com.github.laxika.magicalvibes.cards.c.CraterousStomp;
import com.github.laxika.magicalvibes.cards.c.CircadianStruggle;
import com.github.laxika.magicalvibes.cards.e.ElvishElegy;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.y.ThoughtweftsCall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LimitlessRekindling.class, AquaticSubtlety.class, CraterousStomp.class,
        CircadianStruggle.class, ElvishElegy.class, RiteOfFlame.class, ThoughtweftsCall.class})
class LimitlessRekindlingTest extends BaseCardTest {

    @Test
    void conjuresRandomInstantOrSorceryIntoExileForFreeCastUntilEndOfTurn() {
        harness.setHand(player1, java.util.List.of(new LimitlessRekindling()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        Card exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        assertThat(exiled.getName()).isNotIn("Aquatic Subtlety", "Craterous Stomp", "Limitless Rekindling",
                "Circadian Struggle", "Elvish Elegy", "Rite of Flame", "Thoughtweft's Call");
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(exiled.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(exiled.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(exiled.getId());
    }

    @Test
    void stormConjuresOneAdditionalCardForEachEarlierSpellWithoutCastingTheCopy() {
        harness.setHand(player1, java.util.List.of(new RiteOfFlame(), new LimitlessRekindling()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(2);
        for (Card exiled : gd.getPlayerExiledCards(player1.getId())) {
            assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
            assertThat(gd.exilePlayWithoutPayingManaCost).contains(exiled.getId());
            assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(exiled.getId());
        }
    }
}
