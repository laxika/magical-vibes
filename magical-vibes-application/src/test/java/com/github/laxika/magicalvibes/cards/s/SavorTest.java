package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Savor.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class})
class SavorTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -2/-2 until end of turn and creates a Food token")
    void weakensCreatureAndCreatesFood() {
        Permanent target = addCreature(player2, new HillGiant());
        castSavor(target);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("The Food token created by Savor can be sacrificed for 3 life")
    void createdFoodCanBeSacrificedForLife() {
        Permanent target = addCreature(player2, new HillGiant());
        castSavor(target);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("The -2/-2 effect wears off at end of turn")
    void weakensUntilEndOfTurn() {
        Permanent target = addCreature(player2, new HillGiant());
        castSavor(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The -2/-2 effect can kill a 2/2 creature")
    void killsTwoToughnessCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        castSavor(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new Savor()));
        addSavorMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSavor(Permanent target) {
        harness.setHand(player1, List.of(new Savor()));
        addSavorMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addSavorMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addCreature(Player player, Card creature) {
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
