package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StopCold.class, AirElemental.class, FountainOfYouth.class, Plains.class})
class StopColdTest extends BaseCardTest {

    @Test
    @DisplayName("Stops and attaches to a target creature")
    void stopsCreature() {
        Permanent creature = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new StopCold()));
        addStopColdMana();

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stop Cold")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can stop an artifact")
    void stopsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new StopCold()));
        addStopColdMana();

        harness.castEnchantment(player1, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature loses abilities and does not untap")
    void losesAbilitiesAndDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        creature.tap();

        Permanent aura = new Permanent(new StopCold());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonartifact noncreature permanent")
    void cannotTargetNonArtifactNoncreature() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new StopCold()));
        addStopColdMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Can be cast during an opponent's upkeep and taps only when its trigger resolves")
    void flashDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new StopCold()));
        addStopColdMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An enchanted artifact loses its activated ability and regains it after the Aura leaves")
    void artifactAbilitiesAndUntappingReturnAfterAuraLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new StopCold()));
        addStopColdMana();
        harness.castEnchantment(player1, 0, artifact.getId());
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(artifact.isTapped()).isTrue();

        artifact.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        Permanent aura = findPermanent(player1, "Stop Cold");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        artifact.tap();
        harness.performUntapStep(player1);
        assertThat(artifact.isTapped()).isFalse();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Creature abilities return when the Aura leaves")
    void creatureAbilitiesReturnAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new StopCold()));
        addStopColdMana();
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        Permanent aura = findPermanent(player1, "Stop Cold");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    private void addStopColdMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
