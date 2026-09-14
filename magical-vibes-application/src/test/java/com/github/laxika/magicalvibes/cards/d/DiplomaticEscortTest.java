package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.l.Lunge;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SpectersWail;
import com.github.laxika.magicalvibes.cards.s.StingingBarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiplomaticEscort.class, DartingMerfolk.class, Lunge.class, Mountain.class, SpectersWail.class,
        StingingBarrier.class})
class DiplomaticEscortTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell that targets a creature")
    void countersSpellTargetingCreature() {
        Permanent escort = addCreatureReady(player1, new DiplomaticEscort());
        Permanent target = addCreatureReady(player2, new DartingMerfolk());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Lunge lunge = new Lunge();
        harness.setHand(player2, List.of(lunge));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, List.of(target.getId(), player2.getId()));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, lunge.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Lunge");
        harness.assertOnBattlefield(player2, "Darting Merfolk");
        harness.assertOnBattlefield(player1, "Diplomatic Escort");
        assertThat(escort.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters an activated ability that targets a creature")
    void countersActivatedAbilityTargetingCreature() {
        Permanent escort = addCreatureReady(player1, new DiplomaticEscort());
        Permanent target = addCreatureReady(player1, new DartingMerfolk());
        addCreatureReady(player2, new StingingBarrier());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passPriority(player2);

        UUID abilityId = harness.getGameData().stack.getLast().getCard().getId();
        harness.activateAbility(player1, 0, null, abilityId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darting Merfolk");
        assertThat(escort.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a spell that targets a player")
    void cannotTargetSpellThatTargetsAPlayer() {
        addCreatureReady(player1, new DiplomaticEscort());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        SpectersWail spectersWail = new SpectersWail();
        harness.setHand(player2, List.of(spectersWail));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spectersWail.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new DiplomaticEscort());
        Permanent target = addCreatureReady(player1, new DartingMerfolk());
        addCreatureReady(player2, new StingingBarrier());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passPriority(player2);

        UUID abilityId = harness.getGameData().stack.getLast().getCard().getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }
}
