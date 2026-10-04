package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AcolyteOfAclazotz;
import com.github.laxika.magicalvibes.cards.b.BodyDropper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgeBoss.class, BodyDropper.class, GrizzlyBears.class, AcolyteOfAclazotz.class,
        FountainOfYouth.class})
class ForgeBossTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each opponent when you sacrifice a creature")
    void dealsDamageWhenYouSacrificeCreature() {
        harness.addToBattlefield(player1, new ForgeBoss());
        harness.addToBattlefield(player1, new BodyDropper());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSacrificeMana();

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("Triggers only once each turn and only for sacrificed creatures")
    void triggersOnlyOnceEachTurnForCreatures() {
        harness.addToBattlefield(player1, new ForgeBoss());
        harness.addToBattlefield(player1, new BodyDropper());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSacrificeMana();

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        resolveAllTriggers();

        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, secondSacrifice.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondSacrifice.getCard());
    }

    @Test
    @DisplayName("Does not trigger when you sacrifice a noncreature permanent")
    void doesNotTriggerForNoncreaturePermanent() {
        harness.addToBattlefield(player1, new ForgeBoss());
        Permanent acolyte = harness.addToBattlefieldAndReturn(player1, new AcolyteOfAclazotz());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        acolyte.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, fountain.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @CardUsed({ForgeBoss.class, BodyDropper.class})
    @DisplayName("Does not trigger when Forge Boss itself is sacrificed")
    void doesNotTriggerForItsOwnSacrifice() {
        Permanent boss = harness.addToBattlefieldAndReturn(player1, new ForgeBoss());
        harness.addToBattlefield(player1, new BodyDropper());
        addSacrificeMana();

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, boss.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(boss.getCard());
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed({ForgeBoss.class, BodyDropper.class})
    @DisplayName("An opponent's sacrifice does not trigger Forge Boss")
    void doesNotTriggerForOpponentsSacrifice() {
        harness.addToBattlefield(player1, new ForgeBoss());
        harness.addToBattlefield(player2, new BodyDropper());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new BodyDropper());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, sacrificed.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(sacrificed.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed({ForgeBoss.class, BodyDropper.class})
    @DisplayName("Each Forge Boss triggers independently for the same sacrifice")
    void eachBossTriggersIndependently() {
        harness.addToBattlefield(player1, new ForgeBoss());
        harness.addToBattlefield(player1, new ForgeBoss());
        harness.addToBattlefield(player1, new BodyDropper());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new BodyDropper());
        addSacrificeMana();

        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sacrificing an artifact does not consume the creature trigger for the turn")
    void noncreatureSacrificeDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new ForgeBoss());
        Permanent acolyte = harness.addToBattlefieldAndReturn(player1, new AcolyteOfAclazotz());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new BodyDropper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BodyDropper());
        acolyte.setSummoningSick(false);
        addSacrificeMana();

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, fountain.getId());
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);

        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed({ForgeBoss.class, BodyDropper.class})
    @DisplayName("Can trigger again on the opponent's next turn")
    void triggersAgainOnNextTurn() {
        harness.addToBattlefield(player1, new ForgeBoss());
        harness.addToBattlefield(player1, new BodyDropper());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BodyDropper());
        harness.setLibrary(player2, List.of(new BodyDropper()));
        addSacrificeMana();

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BodyDropper());
        addSacrificeMana();
        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private void addSacrificeMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
