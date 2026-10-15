package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nalfeshnee.class, GrizzlyBears.class, AngelsMercy.class, Stifle.class, Shock.class})
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
        resolveAllTriggers();

        List<Permanent> bearsOnBattlefield = findPermanents(player1, "Grizzly Bears");
        assertThat(bearsOnBattlefield).hasSize(2);
        Permanent token = bearsOnBattlefield.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        resolveAllTriggers();
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
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 14);
    }

    @Test
    void sacrificeAbilityTriggersAgainAfterBeingCountered() {
        harness.addToBattlefield(player1, new Nalfeshnee());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setExile(player1, List.of(bears));
        gd.exilePlayPermissions.put(bears.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bears.getId());
        resolveAllTriggers();
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, gd.stack.getLast().getTargetableId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears")).contains(token).hasSize(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears")).doesNotContain(token).hasSize(1);
    }

    @Test
    void doesNotCopySpellsCastFromHand() {
        harness.addToBattlefield(player1, new Nalfeshnee());
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsSpellsFromExile() {
        harness.addToBattlefield(player1, new Nalfeshnee());
        AngelsMercy mercy = new AngelsMercy();
        harness.setExile(player2, List.of(mercy));
        gd.exilePlayPermissions.put(mercy.getId(), player2.getId());
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player2, mercy.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 7);
    }

    @Test
    void canChooseANewTargetForTheCopy() {
        harness.addToBattlefield(player1, new Nalfeshnee());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setExile(player1, List.of(shock));
        gd.exilePlayPermissions.put(shock.getId(), player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void decliningNewTargetsStillCopiesTheSpell() {
        harness.addToBattlefield(player1, new Nalfeshnee());
        Shock shock = new Shock();
        harness.setExile(player1, List.of(shock));
        gd.exilePlayPermissions.put(shock.getId(), player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

}
