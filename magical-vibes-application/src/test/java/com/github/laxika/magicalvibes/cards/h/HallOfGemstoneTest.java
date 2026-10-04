package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.a.AncientZiggurat;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.i.InfernalDarkness;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HallOfGemstone.class, CrystalVein.class, Forest.class, Island.class, Mountain.class,
        FireDiamond.class, InfernalDarkness.class, AncientZiggurat.class, Incinerate.class})
class HallOfGemstoneTest extends BaseCardTest {
    @Test
    @DisplayName("The world rule removes the older Hall but its resolved effect lasts through the turn")
    void resolvedEffectSurvivesSourceLeavingBattlefield() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent newerHall = harness.enterBattlefieldAndReturn(player2, new HallOfGemstone());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Hall of Gemstone");
        harness.assertInGraveyard(player1, "Hall of Gemstone");
        assertThat(findPermanent(player2, "Hall of Gemstone").getId()).isEqualTo(newerHall.getId());
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
    @Test
    @DisplayName("Changing a land's mana color preserves its spending restrictions")
    void replacedManaRetainsCreatureSpellRestriction() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new AncientZiggurat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isEqualTo(1);
    }
    @Test
    @DisplayName("Nonland mana sources retain their original color")
    void nonlandManaIsNotReplaced() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new FireDiamond());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Lands entering after the upkeep choice also produce the chosen color")
    void newlyEnteredLandProducesChosenColor() {
        harness.addToBattlefield(player1, new HallOfGemstone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.addToBattlefield(player1, new Mountain());
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Sacrificing Crystal Vein still produces two colorless mana")
    void sacrificedLandStillProducesColorlessMana() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new CrystalVein());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertInGraveyard(player1, "Crystal Vein");
        harness.assertNotOnBattlefield(player1, "Crystal Vein");
    }

    @Test
    @DisplayName("Hall of Gemstone does not suppress Infernal Darkness replacing colorless mana")
    void colorlessManaStillReceivesOtherReplacementEffects() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new CrystalVein());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.addToBattlefield(player2, new InfernalDarkness());
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Active player chooses a color and their lands produce it instead of their own")
    void chosenColorReplacesOwnLandMana() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "BLUE");

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The lock is symmetric Ă„â€šĂ‹ÂÄ‚ËĂ˘â‚¬ĹˇĂ‚Â¬Ä‚ËĂ˘â€šÂ¬ÄąÄ„ an opponent's lands produce the chosen color too")
    void chosenColorAppliesToOpponentLands() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not replace colorless mana produced by a land")
    void colorlessManaIsNotReplaced() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new CrystalVein());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The non-controller chooses during their own upkeep")
    void activePlayerChoosesEvenWhenNotTheController() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleListChoice(player2, "RED");
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The color lock wears off at end of turn")
    void lockWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HallOfGemstone());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mountain = findPermanent(player1, "Mountain");
        mountain.untap();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }
}
