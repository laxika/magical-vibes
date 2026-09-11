package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinscBelovedRanger.class, GrizzlyBears.class})
class MinscBelovedRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates legendary Boo")
    void enteringCreatesBoo() {
        harness.setHand(player1, List.of(new MinscBelovedRanger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent boo = findPermanents(player1, "Boo").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(boo.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(boo.getEffectivePower()).isEqualTo(1);
        assertThat(boo.getEffectiveToughness()).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(boo, CardSubtype.HAMSTER)).isTrue();
        assertThat(gqs.hasKeyword(gd, boo, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, boo, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability sets base P/T and adds Giant until end of turn")
    void activatedAbilityChangesTargetUntilEndOfTurn() {
        addCreatureReady(player1, new MinscBelovedRanger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears).contains(CardSubtype.GIANT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears).contains(CardSubtype.GIANT)).isFalse();
    }
}
