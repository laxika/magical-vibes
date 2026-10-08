package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WebShooters.class, GrizzlyBears.class})
class WebShootersTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and reach")
    void equippedCreatureGetsBoostAndReach() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Equip ability attaches Web-Shooters to a creature")
    void equipAbilityAttachesToCreature() {
        Permanent shooters = addWebShootersReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shooters.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature's attack taps a target creature an opponent controls")
    void attackTriggerTapsOpponentCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger only allows creatures controlled by an opponent")
    void attackTriggerRestrictsTargets() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unequipped Web-Shooters do not trigger when a creature attacks")
    void noTriggerWhenUnequipped() {
        addCreatureReady(player1, new GrizzlyBears());
        addWebShootersReady(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof WebShooters);
    }

    @Test
    @DisplayName("The attack ability belongs to the equipped creature")
    void attackAbilityHasCreatureAsSource() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(creature.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The creature's controller chooses targets even when the opponent controls the Equipment")
    void creatureControllerControlsGrantedTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player2);
        shooters.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger still resolves after the Equipment leaves the battlefield")
    void removingEquipmentDoesNotRemovePendingTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(shooters);
        gd.playerGraveyards.get(player1.getId()).add(shooters.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("No attack ability remains on the stack when no opposing creature can be targeted")
    void noTriggerOnStackWithoutLegalTargets() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shooters = addWebShootersReady(player1);
        shooters.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addWebShootersReady(Player player) {
        return addCreatureReady(player, new WebShooters());
    }
}
