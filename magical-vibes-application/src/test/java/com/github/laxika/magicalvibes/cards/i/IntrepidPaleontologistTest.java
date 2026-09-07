package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BelligerentYearling;
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

@CardUsed({IntrepidPaleontologist.class, BelligerentYearling.class, GrizzlyBears.class})
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

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(dinosaur.getId()))
                .findFirst()
                .orElseThrow();
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
        Permanent paleontologist = harness.addToBattlefieldAndReturn(player1, new IntrepidPaleontologist());
        paleontologist.setSummoningSick(false);
        return paleontologist;
    }
}
