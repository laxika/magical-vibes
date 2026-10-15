package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlloyGolem;
import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GalinasKnight;
import com.github.laxika.magicalvibes.cards.s.SparringGolem;
import com.github.laxika.magicalvibes.cards.s.StrengthOfUnity;
import com.github.laxika.magicalvibes.cards.u.UrborgSkeleton;
import com.github.laxika.magicalvibes.cards.v.VodalianSerpent;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TsabosAssassin.class, UrborgSkeleton.class, VodalianSerpent.class, BenalishLancer.class,
        Forest.class, GalinasKnight.class, SparringGolem.class, AlloyGolem.class, StrengthOfUnity.class})
class TsabosAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature sharing the most common color and ignores regeneration")
    void destroysCreatureSharingMostCommonColor() {
        Permanent assassin = addAssassin();
        Permanent target = addCreatureReady(player2, new UrborgSkeleton());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Urborg Skeleton");
        harness.assertInGraveyard(player2, "Urborg Skeleton");
        assertThat(assassin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroys a creature when its color is tied for most common")
    void destroysCreatureWithTiedColor() {
        addAssassin();
        Permanent target = addCreatureReady(player2, new VodalianSerpent());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vodalian Serpent");
        harness.assertInGraveyard(player2, "Vodalian Serpent");
    }

    @Test
    @DisplayName("Allows any creature target but does nothing when the color condition is false")
    void doesNothingWhenTargetDoesNotShareMostCommonColor() {
        addAssassin();
        addCreatureReady(player2, new VodalianSerpent());
        addCreatureReady(player2, new VodalianSerpent());
        Permanent target = addCreatureReady(player2, new BenalishLancer());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Benalish Lancer")).isNotNull();
    }

    @Test
    @DisplayName("Destroys a multicolored creature sharing a tied most common color")
    void destroysMulticoloredCreatureSharingMostCommonColor() {
        addAssassin();
        Permanent target = addCreatureReady(player2, new GalinasKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Galina's Knight");
        harness.assertInGraveyard(player2, "Galina's Knight");
    }

    @Test
    @DisplayName("Does nothing to a colorless creature")
    void doesNothingToColorlessCreature() {
        addAssassin();
        Permanent target = addCreatureReady(player2, new SparringGolem());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sparring Golem");
    }

    @Test
    @DisplayName("Checks the most common color when the ability resolves")
    void checksMostCommonColorAtResolution() {
        addAssassin();
        Permanent target = addCreatureReady(player2, new BenalishLancer());

        harness.activateAbility(player1, 0, null, target.getId());
        addCreatureReady(player1, new VodalianSerpent());
        addCreatureReady(player1, new VodalianSerpent());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("Counts colors supplied by a continuous effect")
    void countsColorsSuppliedByContinuousEffects() {
        addAssassin();
        Permanent target = addCreatureReady(player2, new BenalishLancer());
        addCreatureReady(player1, new VodalianSerpent());
        addCreatureReady(player1, new VodalianSerpent());
        Permanent alloyGolem = addCreatureReady(player1, new AlloyGolem());
        alloyGolem.setChosenColor(CardColor.WHITE);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInGraveyard(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("Can target and destroy a creature controlled by the Assassin's controller")
    void destroysOwnCreature() {
        addAssassin();
        Permanent target = addCreatureReady(player1, new UrborgSkeleton());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urborg Skeleton");
        harness.assertInGraveyard(player1, "Urborg Skeleton");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addAssassin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts every color of a multicolored permanent separately")
    void countsEachColorOfMulticoloredPermanents() {
        addAssassin();
        Permanent target = addCreatureReady(player2, new BenalishLancer());
        addCreatureReady(player1, new GalinasKnight());
        addCreatureReady(player2, new UrborgSkeleton());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInGraveyard(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("Colorless lands do not contribute their mana colors to the count")
    void ignoresManaColorsOfColorlessLands() {
        addAssassin();
        Permanent target = addCreatureReady(player2, new UrborgSkeleton());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Urborg Skeleton");
        harness.assertInGraveyard(player2, "Urborg Skeleton");
    }

    @Test
    @DisplayName("Can destroy a target that only becomes eligible before resolution")
    void destroysTargetThatBecomesEligibleAtResolution() {
        addAssassin();
        addCreatureReady(player2, new UrborgSkeleton());
        Permanent target = addCreatureReady(player2, new BenalishLancer());

        harness.activateAbility(player1, 0, null, target.getId());
        addCreatureReady(player1, new BenalishLancer());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInGraveyard(player2, "Benalish Lancer");
    }

    @Test
    @DisplayName("Counts colored noncreature permanents across both battlefields")
    void countsColoredNoncreaturePermanents() {
        Permanent assassin = addAssassin();
        Permanent target = addCreatureReady(player2, new BenalishLancer());
        addCreatureReady(player1, new VodalianSerpent());
        addCreatureReady(player2, new VodalianSerpent());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfUnity());
        aura.setAttachedTo(assassin.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInGraveyard(player2, "Benalish Lancer");
    }

    private Permanent addAssassin() {
        return addCreatureReady(player1, new TsabosAssassin());
    }
}
