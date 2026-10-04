package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArcanisTheOmnipotent;
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

@CardUsed({EchoStorm.class, FountainOfYouth.class, GrizzlyBears.class, ArcanisTheOmnipotent.class})
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
        Card commander = new ArcanisTheOmnipotent();
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
        Card commander = new ArcanisTheOmnipotent();
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

    @Test
    void countsCommanderCastsWhenCastTriggerResolves() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Card commander = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commander);
        harness.setHand(player1, List.of(new EchoStorm()));
        addMana();
        harness.castSorcery(player1, 0, fountain.getId());

        // Model a commander cast recorded while the cast trigger is still on the stack.
        gd.commanderTaxByCardId.put(commander.getId(), 2);
        resolveRemainingStack();

        assertThat(findPermanents(player1, "Fountain of Youth")).filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void createsOneCopyForEveryPriorCommanderCast() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Card commander = new ArcanisTheOmnipotent();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 6);

        cast(fountain.getId());

        assertThat(findPermanents(player1, "Fountain of Youth")).filteredOn(p -> p.getCard().isToken())
                .hasSize(4);
    }

    @Test
    void copiesOpponentsTappedArtifactUnderSpellControllersControlUntapped() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        fountain.tap();

        cast(fountain.getId());

        assertThat(findPermanents(player1, "Fountain of Youth")).singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.isTapped()).isFalse();
                });
        assertThat(findPermanents(player2, "Fountain of Youth")).containsExactly(fountain);
    }

    @Test
    void createsNoTokenIfTargetLeavesBeforeResolution() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new EchoStorm()));
        addMana();
        harness.castSorcery(player1, 0, fountain.getId());
        gd.playerBattlefields.get(player1.getId()).remove(fountain);
        resolveRemainingStack();

        assertThat(findPermanents(player1, "Fountain of Youth")).isEmpty();
        harness.assertInGraveyard(player1, "Echo Storm");
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
                resolveAllTriggers();
            }
        }
    }
}
