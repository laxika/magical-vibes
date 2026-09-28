package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nalfeshnee.class, GrizzlyBears.class, AngelsMercy.class})
class NalfeshneeTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature spell cast from exile as a hasty token sacrificed at end step")
    void copiesPermanentSpellFromExile() {
        harness.addToBattlefield(player1, new Nalfeshnee());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setExile(player1, List.of(bears));
        gd.exilePlayPermissions.put(bears.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bearsOnBattlefield = findPermanents(player1, "Grizzly Bears");
        assertThat(bearsOnBattlefield).hasSize(2);
        Permanent token = bearsOnBattlefield.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Copies a nonpermanent spell cast from exile without making a token")
    void copiesNonPermanentSpellFromExile() {
        harness.addToBattlefield(player1, new Nalfeshnee());
        AngelsMercy angelsMercy = new AngelsMercy();
        harness.setExile(player1, List.of(angelsMercy));
        gd.exilePlayPermissions.put(angelsMercy.getId(), player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromExile(player1, angelsMercy.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 14);
    }
}
