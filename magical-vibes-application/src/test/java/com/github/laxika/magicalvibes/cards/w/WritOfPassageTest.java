package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({WritOfPassage.class, GrizzlyBears.class, HillGiant.class})
class WritOfPassageTest extends BaseCardTest {

    @Test
    @DisplayName("The attack trigger makes an enchanted small creature unblockable")
    void attackTriggerMakesEnchantedSmallCreatureUnblockable() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does not trigger for an enchanted creature with power 3")
    void attackTriggerRequiresPowerTwoOrLess() {
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks power again when it resolves")
    void attackTriggerRechecksPowerOnResolution() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));
        attacker.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Forecast makes a target small creature unblockable and keeps the card in hand")
    void forecastMakesTargetUnblockableAndKeepsSourceInHand() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        WritOfPassage writ = new WritOfPassage();
        harness.setHand(player1, List.of(writ));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(writ);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WritOfPassage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, firstTarget.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires a creature with power 2 or less during the controller's upkeep")
    void forecastRequiresSmallCreatureAndUpkeep() {
        Permanent largeTarget = addCreatureReady(player2, new HillGiant());
        WritOfPassage writ = new WritOfPassage();
        harness.setHand(player1, List.of(writ));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, largeTarget.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, largeTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAuraOn(Permanent host, com.github.laxika.magicalvibes.model.Player controller) {
        Permanent aura = new Permanent(new WritOfPassage());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
