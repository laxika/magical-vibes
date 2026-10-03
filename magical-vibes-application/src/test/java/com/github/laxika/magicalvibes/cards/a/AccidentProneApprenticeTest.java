package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({AccidentProneApprentice.class, AmphibianAccident.class, FountainOfYouth.class,
        GiantGrowth.class, GrizzlyBears.class, SerraAngel.class})
class AccidentProneApprenticeTest extends BaseCardTest {

    @Test
    void adventureTurnsTargetIntoBlueOneOneFrogWithoutAbilities() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        AccidentProneApprentice card = new AccidentProneApprentice();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(1);
        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, angel)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.FROG);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureTransformationWearsOffAtEndOfTurn() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castAdventure(angel);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, angel)).contains(CardColor.WHITE);
        assertThat(angel.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    void cannotAdventureTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new AccidentProneApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void noncreatureSpellPerpetuallyBoostsApprenticeOnTheBattlefield() {
        AccidentProneApprentice apprentice = new AccidentProneApprentice();
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, apprentice);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
    }

    @Test
    void creatureSpellDoesNotTriggerThePerpetualBoost() {
        AccidentProneApprentice apprentice = new AccidentProneApprentice();
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, apprentice);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(1);
    }

    @Test
    void noncreatureSpellPerpetuallyBoostsApprenticeWhileInExile() {
        AccidentProneApprentice apprentice = new AccidentProneApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(apprentice));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, apprentice.getId());
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Accident-Prone Apprentice"));
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotBoostBattlefieldOrExiledApprentice() {
        Permanent battlefieldApprentice = harness.enterBattlefieldAndReturn(player1,
                new AccidentProneApprentice());
        AccidentProneApprentice exiledApprentice = new AccidentProneApprentice();
        harness.setExile(player1, List.of(exiledApprentice));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, battlefieldApprentice.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, battlefieldApprentice)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, battlefieldApprentice)).isEqualTo(4);

        gd.removeFromExile(exiledApprentice.getId());
        Permanent returned = harness.enterBattlefieldAndReturn(player1, exiledApprentice);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
    }

    @Test
    void artifactSpellBoostsApprenticeInExileWithoutAdventurePermission() {
        AccidentProneApprentice apprentice = new AccidentProneApprentice();
        harness.setExile(player1, List.of(apprentice));
        harness.setHand(player1, List.of(new FountainOfYouth()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        gd.removeFromExile(apprentice.getId());
        Permanent returned = harness.enterBattlefieldAndReturn(player1, apprentice);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }

    @Test
    void adventureDoesNotTriggerItsOwnAbilityFromHand() {
        AccidentProneApprentice apprentice = new AccidentProneApprentice();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(apprentice));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, apprentice.getId());
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Accident-Prone Apprentice"));
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
    }

    @Test
    void castingAnotherApprenticesAdventureTriggersBattlefieldApprentice() {
        Permanent apprentice = harness.enterBattlefieldAndReturn(player1,
                new AccidentProneApprentice());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAdventure(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void adventurePreservesEarlierPowerToughnessBoosts() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        castAdventure(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    private void castAdventure(Permanent target) {
        harness.setHand(player1, List.of(new AccidentProneApprentice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
