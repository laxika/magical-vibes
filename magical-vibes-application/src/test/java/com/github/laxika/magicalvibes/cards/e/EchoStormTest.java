package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoStorm.class, FountainOfYouth.class, GrizzlyBears.class})
class EchoStormTest extends BaseCardTest {

    @Test
    void createsTokenCopyOfTargetArtifact() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        cast(fountain.getId());

        assertThat(findPermanents(player1, "Fountain of Youth")).hasSize(2);
        assertThat(findPermanents(player1, "Fountain of Youth")).filteredOn(p -> p.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void copiesSpellForEachCommanderCastFromCommandZone() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 2);

        cast(fountain.getId());

        assertThat(findPermanents(player1, "Fountain of Youth")).filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
        assertThat(findPermanents(player1, "Fountain of Youth")).hasSize(3);
    }

    @Test
    void mayChooseNewTargetForCommanderCopy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 2);

        harness.setHand(player1, List.of(new EchoStorm()));
        addMana();
        harness.castSorcery(player1, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        resolveRemainingStack();

        assertThat(findPermanents(player1, "Fountain of Youth")).hasSize(4);
        assertThat(findPermanents(player1, "Fountain of Youth")).filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> cast(creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new EchoStorm()));
        addMana();
        harness.castSorcery(player1, 0, targetId);
        resolveRemainingStack();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void resolveRemainingStack() {
        while (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()) {
            if (gd.interaction.isAwaitingInput()) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                harness.passBothPriorities();
            }
        }
    }
}
