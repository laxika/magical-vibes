package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VladimirAndGodfrey.class, FyndhornElves.class, GrizzlyBears.class})
class VladimirAndGodfreyTest extends BaseCardTest {

    @Test
    void returnsTappedAndPerpetuallyGetsPlusOnePlusOne() {
        Card vladimirAndGodfrey = new VladimirAndGodfrey();
        harness.setGraveyard(player1, List.of(vladimirAndGodfrey));
        harness.addToBattlefield(player1, new FyndhornElves());
        addMana();
        prepareMainPhase();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(vladimirAndGodfrey.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    void cannotActivateWithoutControllingAOneOneCreature() {
        harness.setGraveyard(player1, List.of(new VladimirAndGodfrey()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addMana();
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control a 1/1 creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
