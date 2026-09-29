package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpendingFlux.class, GrizzlyBears.class})
class ImpendingFluxTest extends BaseCardTest {

    @Test
    void dealsTwoDamageAfterOneOutsideHandSpellToOpponentsAndTheirCreatures() {
        harness.setLife(player2, 20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears outsideHandSpell = new GrizzlyBears();
        gd.addToExile(player1.getId(), outsideHandSpell);
        gd.exilePlayPermissions.put(outsideHandSpell.getId(), player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, outsideHandSpell.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ImpendingFlux()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    void foretellCastCountsImpendingFluxAsOutsideHandSpell() {
        harness.setLife(player2, 20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        ImpendingFlux spell = new ImpendingFlux();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }
}
