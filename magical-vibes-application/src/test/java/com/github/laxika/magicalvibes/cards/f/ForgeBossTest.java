package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AcolyteOfAclazotz;
import com.github.laxika.magicalvibes.cards.b.BodyDropper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    private void addSacrificeMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
