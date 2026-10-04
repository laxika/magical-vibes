package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ExpendableLackey.class)
class ExpendableLackeyTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles Expendable Lackey and creates an unblockable Fish")
    void createsUnblockableFish() {
        ExpendableLackey lackey = new ExpendableLackey();
        harness.setGraveyard(player1, List.of(lackey));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(lackey);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lackey);

        harness.passBothPriorities();

        Permanent fish = findPermanent(player1, "Fish");
        assertThat(fish.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(fish.getCard().getSubtypes()).containsExactly(CardSubtype.FISH);
        assertThat(gqs.getEffectivePower(gd, fish)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fish)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, fish)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability can only be activated as a sorcery")
    void abilityIsSorcerySpeedOnly() {
        harness.setGraveyard(player1, List.of(new ExpendableLackey()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        ExpendableLackey lackey = new ExpendableLackey();
        harness.setGraveyard(player1, List.of(lackey));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lackey);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(lackey);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        ExpendableLackey lackey = new ExpendableLackey();
        harness.setGraveyard(player1, List.of(lackey));
        harness.setHand(player1, List.of(new ExpendableLackey()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lackey);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(lackey);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        ExpendableLackey lackey = new ExpendableLackey();
        harness.setGraveyard(player1, List.of(lackey));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lackey);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(lackey);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateDuringPostcombatMainPhase() {
        harness.setGraveyard(player1, List.of(new ExpendableLackey()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Fish");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
