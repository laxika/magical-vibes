package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.ForecastingFortuneTeller;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
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

@CardUsed({AirbendersReversal.class, ForecastingFortuneTeller.class, ControlMagic.class})
class AirbendersReversalTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys an attacking creature")
    void destroyModeDestroysAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ForecastingFortuneTeller());
        attacker.setAttacking(true);

        cast(0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Forecasting Fortune Teller");
    }

    @Test
    @DisplayName("The airbend mode exiles a creature you control")
    void airbendModeExilesOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ForecastingFortuneTeller());

        cast(1, creature.getId());

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(creature.getOriginalCard().getId()))
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Each mode enforces its own target restriction")
    void modesRejectIllegalTargets() {
        Permanent nonAttackingCreature = harness.addToBattlefieldAndReturn(player2, new ForecastingFortuneTeller());
        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{0}, List.of(nonAttackingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();
        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 1, new int[]{1}, List.of(nonAttackingCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("An airbent creature can be cast for two generic mana")
    void airbentCreatureCanBeCastForGenericMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ForecastingFortuneTeller());
        cast(1, creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, creature.getOriginalCard().getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forecasting Fortune Teller");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getOriginalCard().getId());
        harness.assertOnBattlefield(player1, "Clue");
    }

    @Test
    @DisplayName("Airbend does not waive the two-mana casting cost")
    void airbentCreatureRequiresTwoMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ForecastingFortuneTeller());
        cast(1, creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Forecasting Fortune Teller");
    }

    @Test
    @DisplayName("Airbend does not allow a creature to be cast during combat")
    void airbentCreatureRetainsNormalTiming() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ForecastingFortuneTeller());
        cast(1, creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Airbend grants casting permission to the owner of a borrowed creature")
    void airbendPermissionBelongsToOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ForecastingFortuneTeller());
        harness.addToBattlefieldAndReturn(player1, new ControlMagic()).setAttachedTo(creature.getId());
        harness.runStateBasedActions();

        cast(1, creature.getId());

        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(creature.getOriginalCard().getId()))
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The destroy mode does not destroy a creature that stopped attacking")
    void destroyModeRechecksAttackingRestriction() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ForecastingFortuneTeller());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();
        harness.castModalInstant(player1, 0, 0, List.of(attacker.getId()));

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forecasting Fortune Teller");
        harness.assertNotInGraveyard(player2, "Forecasting Fortune Teller");
        harness.assertInGraveyard(player1, "Airbender's Reversal");
    }

    @Test
    @DisplayName("The airbend mode does not exile a creature whose control changed")
    void airbendModeRechecksController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ForecastingFortuneTeller());
        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();
        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));

        harness.addToBattlefieldAndReturn(player2, new ControlMagic()).setAttachedTo(creature.getId());
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forecasting Fortune Teller");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
        harness.assertInGraveyard(player1, "Airbender's Reversal");
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AirbendersReversal()));
        addMana();
        harness.castModalInstant(player1, 0, mode, List.of(targetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
