package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AstrologiansPlanisphere.class, GrizzlyBears.class, Shock.class})
class AstrologiansPlanisphereTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates a Hero token, attaches the Equipment, and makes it a Wizard")
    void jobSelectCreatesAndEquipsWizardHero() {
        harness.castFromHand(player1, new AstrologiansPlanisphere(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent planisphere = findPermanent(player1, "Astrologian's Planisphere");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(planisphere.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Casting a noncreature spell puts a +1/+1 counter on the equipped creature")
    void noncreatureSpellAddsCounter() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player1);
        planisphere.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The equipped creature gets a counter on its third draw of the turn only once")
    void thirdDrawAddsOneCounter() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player1);
        planisphere.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw(player1);
        resolveTopOfStack();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        draw(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted abilities stop when the Equipment is unattached")
    void noGrantedAbilitiesWhenUnattached() {
        addPlanisphereReady(player1);
        addCreatureReady(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        draw(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attachingAfterThirdDrawDoesNotTriggerOnFourthDraw() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        draw(player1);
        planisphere.setAttachedTo(creature.getId());
        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attachingAfterSecondDrawTriggersOnThirdDraw() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        planisphere.setAttachedTo(creature.getId());
        draw(player1);
        resolveTopOfStack();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void twoPlanispheresEachTriggerOnThirdDraw() {
        Permanent first = addPlanisphereReady(player1);
        Permanent second = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player1);
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        draw(player1);

        assertThat(gd.stack).hasSize(2);
        resolveTopOfStack();
        resolveTopOfStack();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureSpellDoesNotAddCounter() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player1);
        planisphere.setAttachedTo(creature.getId());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void drawTriggerStillCountersOriginalCreatureAfterEquipmentMoves() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent original = addCreatureReady(player1);
        Permanent replacement = addCreatureReady(player1);
        planisphere.setAttachedTo(original.getId());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        draw(player1);
        planisphere.setAttachedTo(replacement.getId());
        resolveTopOfStack();

        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.effectiveCreatureSubtypes(gd, original)).doesNotContain(CardSubtype.WIZARD);
        assertThat(gqs.effectiveCreatureSubtypes(gd, replacement)).contains(CardSubtype.WIZARD);
    }

    @Test
    void equipAbilityAttachesForTwoMana() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(planisphere.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.BEAR, CardSubtype.WIZARD);
    }

    @Test
    void grantedAbilityUsesCreatureControllerRatherThanEquipmentController() {
        Permanent planisphere = addPlanisphereReady(player1);
        Permanent creature = addCreatureReady(player2);
        planisphere.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addPlanisphereReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AstrologiansPlanisphere());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
