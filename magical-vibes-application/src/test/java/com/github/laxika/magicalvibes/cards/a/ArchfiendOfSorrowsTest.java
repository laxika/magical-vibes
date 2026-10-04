package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.t.TragicFall;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchfiendOfSorrows.class, GrizzlyBears.class, MassOfGhouls.class, TragicFall.class})
class ArchfiendOfSorrowsTest extends BaseCardTest {

    @Test
    void etbDebuffsOnlyOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new ArchfiendOfSorrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-2);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void unearthReturnsWithHasteAndExilesAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new ArchfiendOfSorrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent archfiend = findPermanent(player1, "Archfiend of Sorrows");
        assertThat(gqs.hasKeyword(gd, archfiend, Keyword.HASTE)).isTrue();

        harness.passUntil(TurnStep.DECLARE_ATTACKERS);
        gs.declareAttackers(gd, player1, List.of());
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Archfiend of Sorrows");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Archfiend of Sorrows"));
    }

    @Test
    void unearthTriggersDebuffAndKillsTwoToughnessCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new ArchfiendOfSorrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Archfiend of Sorrows");
    }

    @Test
    void debuffDoesNotAffectCreaturesEnteringLaterAndExpiresAtCleanup() {
        Permanent affected = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new ArchfiendOfSorrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(laterCreature.getToughnessModifier()).isZero();
        assertThat(affected.getToughnessModifier()).isEqualTo(-2);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(affected.getPowerModifier()).isZero();
        assertThat(affected.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void unearthedCreatureIsExiledInsteadOfDying() {
        harness.setGraveyard(player1, List.of(new ArchfiendOfSorrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent archfiend = findPermanent(player1, "Archfiend of Sorrows");
        harness.setHand(player1, List.of(new TragicFall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, archfiend.getId());

        harness.assertNotOnBattlefield(player1, "Archfiend of Sorrows");
        harness.assertNotInGraveyard(player1, "Archfiend of Sorrows");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Archfiend of Sorrows"));
    }

    @Test
    void unearthCannotBeActivatedOutsideMainPhase() {
        harness.setGraveyard(player1, List.of(new ArchfiendOfSorrows()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Archfiend of Sorrows");
        harness.assertNotOnBattlefield(player1, "Archfiend of Sorrows");
    }
}
