package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossessedCentaur.class, GrizzlyBears.class, SavannahLions.class, Spellbook.class})
class PossessedCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold gives Possessed Centaur +1/+1, makes it black, and grants its ability")
    void thresholdChangesCreatureAndGrantsAbility() {
        fillGraveyard(player1, 7);
        Permanent centaur = addReadyCentaur();

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, centaur)).containsExactly(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, centaur)).hasSize(1);
    }

    @Test
    @DisplayName("Threshold ability destroys a target green creature")
    void abilityDestroysGreenCreature() {
        fillGraveyard(player1, 7);
        addReadyCentaur();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Threshold ability cannot target a nongreen creature")
    void abilityCannotTargetNongreenCreature() {
        fillGraveyard(player1, 7);
        addReadyCentaur();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SavannahLions());

        prepareActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Threshold bonuses and ability disappear below seven cards in the graveyard")
    void thresholdDisappearsBelowSevenCards() {
        fillGraveyard(player1, 7);
        Permanent centaur = addReadyCentaur();
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, centaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, centaur)).doesNotContain(CardColor.BLACK);
        assertThat(gs.getEffectiveActivatedAbilities(gd, centaur)).isEmpty();
    }

    private Permanent addReadyCentaur() {
        Permanent centaur = harness.addToBattlefieldAndReturn(player1, new PossessedCentaur());
        centaur.setSummoningSick(false);
        return centaur;
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }
}
