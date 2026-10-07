package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EarthVillageRuffians;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

@CardUsed({SozinsComet.class, EarthVillageRuffians.class})
class SozinsCometTest extends BaseCardTest {

    @Test
    void grantsFirebendingFiveToOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        ownCreature.setSummoningSick(false);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new EarthVillageRuffians());
        opposingCreature.tap();

        harness.setHand(player1, List.of(new SozinsComet()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ownCreature.hasKeyword(Keyword.FIREBENDING)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.FIREBENDING)).isFalse();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void grantsFirebendingOnlyToCreaturesPresentWhenItResolves() {
        Permanent earlyCreature = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());

        harness.setHand(player1, List.of(new SozinsComet()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());

        assertThat(earlyCreature.hasKeyword(Keyword.FIREBENDING)).isTrue();
        assertThat(lateCreature.hasKeyword(Keyword.FIREBENDING)).isFalse();
        assertThat(earlyCreature.getTemporaryTriggeredEffects(EffectSlot.ON_ATTACK))
                .hasSize(1);
        assertThat(lateCreature.getTemporaryTriggeredEffects(EffectSlot.ON_ATTACK))
                .isEmpty();
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EarthVillageRuffians());
        harness.setHand(player1, List.of(new SozinsComet()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.FIREBENDING)).isFalse();
        assertThat(creature.getTemporaryTriggeredEffects(EffectSlot.ON_ATTACK))
                .isEmpty();
    }

    @Test
    void foretellsAndCastsFromExile() {
        SozinsComet comet = new SozinsComet();
        harness.setHand(player1, List.of(comet));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(comet.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, comet.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sozin's Comet");
    }

    @Test
    void twoCometsGrantTwoIndependentFirebendingAbilities() {
        addCreatureReady(player1, new EarthVillageRuffians());
        harness.setHand(player1, List.of(new SozinsComet(), new SozinsComet()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);
        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(2);
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(10);
    }

    @Test
    void everyAttackingCreatureProducesFiveMana() {
        addCreatureReady(player1, new EarthVillageRuffians());
        addCreatureReady(player1, new EarthVillageRuffians());
        harness.setHand(player1, List.of(new SozinsComet()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        declareAttackers(List.of(0, 1));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(10);
    }

    @Test
    void foretellCostIsTwoGenericAndOneRed() {
        SozinsComet comet = new SozinsComet();
        addCreatureReady(player1, new EarthVillageRuffians());
        harness.setHand(player1, List.of(comet));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, comet.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        SozinsComet comet = new SozinsComet();
        harness.setHand(player1, List.of(comet));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, comet.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(comet.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }
}
