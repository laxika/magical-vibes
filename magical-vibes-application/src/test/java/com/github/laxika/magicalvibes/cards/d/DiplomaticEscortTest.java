package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BallynockTrapper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiplomaticEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell that targets a creature")
    void countersSpellTargetingCreature() {
        Permanent escort = addCreatureReady(player1, new DiplomaticEscort());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Diplomatic Escort");
        assertThat(escort.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters an activated ability that targets a creature")
    void countersActivatedAbilityTargetingCreature() {
        Permanent escort = addCreatureReady(player1, new DiplomaticEscort());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new BallynockTrapper());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passPriority(player2);

        UUID abilityId = harness.getGameData().stack.getLast().getCard().getId();
        harness.activateAbility(player1, 0, null, abilityId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(escort.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a spell that does not target a creature")
    void cannotTargetSpellThatDoesNotTargetCreature() {
        addCreatureReady(player1, new DiplomaticEscort());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new DiplomaticEscort());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
