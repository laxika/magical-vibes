package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeatherheadSwampStalker.class, FountainOfYouth.class, GrizzlyBears.class, Spellbook.class})
class LeatherheadSwampStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a hexproof counter")
    void entersWithHexproofCounter() {
        Permanent leatherhead = castLeatherhead();

        assertThat(leatherhead.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting the combat trigger removes a hexproof counter and destroys an artifact or enchantment")
    void acceptsCombatTriggerAndDestroysArtifactOrEnchantment() {
        Permanent leatherhead = addLeatherhead(player1);
        leatherhead.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(leatherhead.getCounterCount(CounterType.HEXPROOF)).isZero();
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("The combat trigger only offers artifacts or enchantments controlled by the damaged player")
    void onlyOffersValidPermanents() {
        Permanent leatherhead = addLeatherhead(player1);
        leatherhead.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent damagedPlayerArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent damagedPlayerCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(damagedPlayerArtifact.getId())
                .doesNotContain(ownArtifact.getId(), damagedPlayerCreature.getId());
    }

    @Test
    @DisplayName("Declining the combat trigger preserves the hexproof counter and permanent")
    void declineCombatTrigger() {
        Permanent leatherhead = addLeatherhead(player1);
        leatherhead.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(leatherhead.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    void canRemoveAnotherKindOfCounterAndDestroyAfterCombatEnds() {
        Permanent leatherhead = addLeatherhead(player1);
        leatherhead.setCounterCount(CounterType.HEXPROOF, 0);
        leatherhead.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        leatherhead.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, artifact.getId());
        leatherhead.setAttacking(false);
        resolveAllTriggers();

        assertThat(leatherhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    void choosesWhichKindOfCounterToRemove() {
        Permanent leatherhead = addLeatherhead(player1);
        leatherhead.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        leatherhead.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Remove 1 +1/+1 counters");
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(leatherhead.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(leatherhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Spellbook");
    }

    private Permanent castLeatherhead() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LeatherheadSwampStalker()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Leatherhead, Swamp Stalker");
    }

    private Permanent addLeatherhead(Player player) {
        Permanent leatherhead = addCreatureReady(player, new LeatherheadSwampStalker());
        leatherhead.setCounterCount(CounterType.HEXPROOF, 1);
        return leatherhead;
    }
}
