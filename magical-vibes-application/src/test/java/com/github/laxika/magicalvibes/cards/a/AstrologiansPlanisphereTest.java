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
        harness.setHand(player1, List.of(new AstrologiansPlanisphere()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
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
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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

    private Permanent addPlanisphereReady(Player player) {
        Permanent permanent = new Permanent(new AstrologiansPlanisphere());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
