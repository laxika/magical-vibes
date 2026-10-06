package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BelligerentYearling;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrepidPaleontologist.class, BelligerentYearling.class, GrizzlyBears.class, DeepFreeze.class})
class IntrepidPaleontologistTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        addReadyPaleontologist();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles a target card from a graveyard and tracks it with the source")
    void exilesTargetAndTracksIt() {
        Permanent paleontologist = addReadyPaleontologist();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, bear.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bear.getId())).isNotNull();
        assertThat(gd.findExiledCard(bear.getId()).sourcePermanentId()).isEqualTo(paleontologist.getId());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casts an exiled Dinosaur for its normal cost with a finality counter")
    void castsExiledDinosaurWithFinalityCounter() {
        Permanent paleontologist = addReadyPaleontologist();
        BelligerentYearling dinosaur = new BelligerentYearling();
        harness.setGraveyard(player1, List.of(dinosaur));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, dinosaur.getId());
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Belligerent Yearling");
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.findExiledCard(dinosaur.getId())).isNull();
        assertThat(gd.getCardsExiledByPermanent(paleontologist.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast a non-Dinosaur exiled with this creature")
    void cannotCastNonDinosaur() {
        addReadyPaleontologist();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, bear.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    private Permanent addReadyPaleontologist() {
        return addCreatureReady(player1, new IntrepidPaleontologist());
    }

    @Test
    void canExileOpponentsDinosaurButCannotCastIt() {
        addReadyPaleontologist();
        Card dinosaur = new BelligerentYearling();
        harness.setGraveyard(player2, List.of(dinosaur));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Belligerent Yearling");
        assertThat(gd.findExiledCard(dinosaur.getId())).isNotNull();
        prepareDinosaurCast();
        assertThatThrownBy(() -> harness.castFromExile(player1, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void cannotCastAfterSourceLeavesEvenWithAnotherPaleontologist() {
        Permanent source = addReadyPaleontologist();
        Card dinosaur = exileOwnDinosaur();
        source.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Intrepid Paleontologist");
        addReadyPaleontologist();

        prepareDinosaurCast();
        assertThatThrownBy(() -> harness.castFromExile(player1, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void finalityStillAppliesWhenSourceLeavesBeforeSpellResolves() {
        Permanent source = addReadyPaleontologist();
        Card dinosaur = exileOwnDinosaur();
        prepareDinosaurCast();
        harness.castFromExile(player1, dinosaur.getId());
        source.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Belligerent Yearling");
        assertThat(entered.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        entered.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Belligerent Yearling");
        harness.assertNotInGraveyard(player1, "Belligerent Yearling");
        assertThat(gd.findExiledCard(dinosaur.getId())).isNotNull();
        assertThat(gd.findExiledCard(dinosaur.getId()).sourcePermanentId()).isNull();
    }

    @Test
    void cannotCastWhenPaleontologistLosesItsAbilities() {
        Permanent source = addReadyPaleontologist();
        Card dinosaur = exileOwnDinosaur();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, source.getId());
        harness.passBothPriorities();

        prepareDinosaurCast();
        assertThatThrownBy(() -> harness.castFromExile(player1, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void exileAbilityDoesNotRequireTappingOrHaste() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new IntrepidPaleontologist());
        source.setSummoningSick(true);
        source.tap();
        Card dinosaur = exileOwnDinosaur();

        assertThat(gd.findExiledCard(dinosaur.getId())).isNotNull();
        assertThat(source.isTapped()).isTrue();
    }

    private Card exileOwnDinosaur() {
        Card dinosaur = new BelligerentYearling();
        harness.setGraveyard(player1, List.of(dinosaur));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, dinosaur.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        return dinosaur;
    }

    private void prepareDinosaurCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
