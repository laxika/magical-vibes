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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castAndResolveInstant(player1, 0);

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

    @Test
    void dealsOneDamageWithoutOutsideHandCastsAndSparesController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImpendingFlux()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void countsEveryOutsideHandCastButNotAdditionalHandCasts() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrizzlyBears(), new ImpendingFlux()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        for (int i = 0; i < 2; i++) {
            GrizzlyBears spell = new GrizzlyBears();
            gd.addToExile(player1.getId(), spell);
            gd.exilePlayPermissions.put(spell.getId(), player1.getId());
            harness.castFromExile(player1, spell.getId());
            harness.passBothPriorities();
        }
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3)
                .allSatisfy(permanent -> assertThat(permanent.getMarkedDamage()).isZero());
    }

    @Test
    void cannotCastForetoldCardOnTheTurnItWasForetold() {
        ImpendingFlux spell = new ImpendingFlux();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }
}
