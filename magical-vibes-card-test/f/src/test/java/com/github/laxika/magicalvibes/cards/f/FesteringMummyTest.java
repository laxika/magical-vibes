package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.cards.s.SacredCat;
import com.github.laxika.magicalvibes.cards.s.ScribeOfTheMindful;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FesteringMummy.class, ScribeOfTheMindful.class, SacredCat.class, PouncingCheetah.class, Island.class})
class FesteringMummyTest extends BaseCardTest {

    /**
     * Sets up combat where Festering Mummy (player1, 1/1) attacks and is blocked by a 3/2 creature
     * (player2). Festering Mummy dies from combat damage.
     */
    private void setupCombatWhereMummyDies() {
        Permanent mummyPerm = findPermanent(player1, "Festering Mummy");
        mummyPerm.setSummoningSick(false);
        mummyPerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new PouncingCheetah());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When Festering Mummy dies, controller is prompted to choose a target creature (CR 603.3d)")
    void deathTriggerPromptsTargetChoice() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new ScribeOfTheMindful());
        setupCombatWhereMummyDies();

        harness.passBothPriorities(); // Combat damage — Mummy dies, target selection prompt

        harness.assertInGraveyard(player1, "Festering Mummy");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may puts a -1/-1 counter on the target 2/2, reducing it to 1/1")
    void deathTriggerPutsCounterOnTarget() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new ScribeOfTheMindful());

        UUID targetId = harness.getPermanentId(player2, "Scribe of the Mindful");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, targetId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // Accept may -> effect resolves

        Permanent target = findPermanent(player2, "Scribe of the Mindful");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("A -1/-1 counter kills a 1/1 target creature")
    void deathTriggerKillsOneOneCreature() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new SacredCat());

        UUID catId = harness.getPermanentId(player2, "Sacred Cat");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, catId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, true); // Accept may -> effect resolves

        harness.assertNotOnBattlefield(player2, "Sacred Cat");
        harness.assertInGraveyard(player2, "Sacred Cat");
    }

    @Test
    @DisplayName("Declining the may leaves the target creature untouched")
    void decliningMayDoesNotPutCounter() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new ScribeOfTheMindful());

        UUID targetId = harness.getPermanentId(player2, "Scribe of the Mindful");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, targetId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, false); // Decline may

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Festering Mummy"));

        Permanent target = findPermanent(player2, "Scribe of the Mindful");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Triggered ability fizzles when the target creature leaves before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new FesteringMummy());
        harness.addToBattlefield(player2, new ScribeOfTheMindful());

        UUID targetId = harness.getPermanentId(player2, "Scribe of the Mindful");

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Mummy dies, target selection

        harness.handlePermanentChosen(player1, targetId); // Choose target -> ability on stack

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities(); // Resolve — target gone, fizzles

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger offers creatures of either controller but excludes lands and the dead source")
    void targetsOnlyLivingCreaturesOfEitherController() {
        harness.addToBattlefield(player1, new FesteringMummy());
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new ScribeOfTheMindful());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SacredCat());
        harness.addToBattlefield(player2, new Island());
        setupCombatWhereMummyDies();
        UUID blockerId = harness.getPermanentId(player2, "Pouncing Cheetah");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(friendly.getId(), opposing.getId(), blockerId);
        harness.handlePermanentChosen(player1, friendly.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(friendly.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, friendly)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death with no creature targets produces no ability or optional choice")
    void noCreatureTargetsSkipsDeathAbility() {
        Permanent mummy = harness.addToBattlefieldAndReturn(player1, new FesteringMummy());
        harness.addToBattlefield(player1, new Island());
        mummy.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Festering Mummy");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({OneWithTheStars.class})
    @DisplayName("Death ability does not resolve when its target stops being a creature")
    void abilityFizzlesWhenTargetStopsBeingCreature() {
        harness.addToBattlefield(player1, new FesteringMummy());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScribeOfTheMindful());
        setupCombatWhereMummyDies();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new OneWithTheStars());
        aura.setAttachedTo(target.getId());
        assertThat(gqs.isCreature(gd, target)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
