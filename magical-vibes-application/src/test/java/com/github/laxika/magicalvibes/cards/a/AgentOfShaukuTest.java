package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.cards.z.ZerapaMinotaur;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgentOfShauku.class, WintermoonMesa.class, ZerapaMinotaur.class})
class AgentOfShaukuTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives the target creature +2/+0")
    void sacrificeLandBoostsTargetCreature() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        harness.addToBattlefield(player1, new WintermoonMesa());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        harness.addToBattlefield(player1, new WintermoonMesa());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZerapaMinotaur());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's land to pay the cost")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        harness.addToBattlefield(player2, new WintermoonMesa());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a land");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Cannot activate without the required black mana")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("The boost does not happen if the target leaves before resolution")
    void boostDoesNotHappenIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new AgentOfShauku());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZerapaMinotaur());
        prepareForActivation();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
    }

    private void prepareForActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
