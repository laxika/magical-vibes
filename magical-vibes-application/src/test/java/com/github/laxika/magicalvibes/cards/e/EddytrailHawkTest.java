package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EddytrailHawk.class, DukharaPeafowl.class})
class EddytrailHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two energy counters")
    void entersWithTwoEnergyCounters() {
        harness.setHand(player1, List.of(new EddytrailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay energy to give another attacking creature flying")
    void paysEnergyToGrantFlying() {
        addCreatureReady(player1, new EddytrailHawk());
        Permanent target = addCreatureReady(player1, new DukharaPeafowl());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot give flying without enough energy")
    void cannotPayWithoutEnoughEnergy() {
        addCreatureReady(player1, new EddytrailHawk());
        Permanent target = addCreatureReady(player1, new DukharaPeafowl());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Eddytrail Hawk itself")
    void cannotTargetItself() {
        Permanent hawk = addCreatureReady(player1, new EddytrailHawk());
        addCreatureReady(player1, new DukharaPeafowl());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger when attacking alone")
    void noTriggerWhenAttackingAlone() {
        addCreatureReady(player1, new EddytrailHawk());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining payment preserves energy and does not grant flying")
    void declinesEnergyPayment() {
        addCreatureReady(player1, new EddytrailHawk());
        Permanent target = addCreatureReady(player1, new DukharaPeafowl());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonattackingCreature() {
        addCreatureReady(player1, new EddytrailHawk());
        addCreatureReady(player1, new DukharaPeafowl());
        Permanent nonattacker = addCreatureReady(player1, new DukharaPeafowl());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonattacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying expires at end of turn and paying spends exactly one energy")
    void flyingExpiresAtEndOfTurn() {
        addCreatureReady(player1, new EddytrailHawk());
        Permanent target = addCreatureReady(player1, new DukharaPeafowl());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

}
