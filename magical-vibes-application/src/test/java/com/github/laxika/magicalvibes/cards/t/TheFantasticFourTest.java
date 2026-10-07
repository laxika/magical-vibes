package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BeastOfBurden;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LukeCageHeroForHire;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFantasticFour.class, LukeCageHeroForHire.class, GrizzlyBears.class,
        BeastOfBurden.class, HillGiant.class, PhyrexianHulk.class})
class TheFantasticFourTest extends BaseCardTest {

    private static final String WALL = "Create a 0/4 colorless Wall creature token with defender";
    private static final String DAMAGE = "The Fantastic Four deal 3 damage to each opponent";
    private static final String COUNTERS = "Put two +1/+1 counters on target creature";
    private static final String DRAW = "Draw a card";

    @Test
    void ownEntryOffersModesAndCreatesWall() {
        harness.castFromHand(player1, new TheFantasticFour(), "{R}{G}{W}{U}");
        awaitModeChoice();
        harness.handleListChoice(player1, WALL);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wall")).isEqualTo(1);
    }

    @Test
    void qualifyingSpellTriggersDamageAndConsumesThatModeForTheTurn() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        harness.setHand(player1, List.of(new LukeCageHeroForHire(), new LukeCageHeroForHire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        awaitModeChoice();
        harness.handleListChoice(player1, DAMAGE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);

        harness.castCreature(player1, 0);
        awaitModeChoice();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(DAMAGE);
        harness.handleListChoice(player1, DRAW);
        resolveAllTriggers();
    }

    @Test
    void qualifyingSpellOffersCounterModeWithCreatureTarget() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new LukeCageHeroForHire(), "{2}{R}{R}");
        awaitModeChoice();
        harness.handleListChoice(player1, COUNTERS);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void qualifyingSpellOffersDrawMode() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new LukeCageHeroForHire(), "{2}{R}{R}");
        awaitModeChoice();
        harness.handleListChoice(player1, DRAW);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void nonQualifyingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    private void awaitModeChoice() {
        for (int i = 0; i < 4; i++) {
            if (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) != null) {
                return;
            }
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    void manaValueAloneCanQualifyASpell() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        int opponentLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        awaitModeChoice();
        harness.handleListChoice(player1, DAMAGE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void toughnessAloneCanQualifyASpell() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        int opponentLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new PhyrexianHulk(), "{6}");
        awaitModeChoice();
        harness.handleListChoice(player1, DAMAGE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
        harness.assertOnBattlefield(player1, "Phyrexian Hulk");
    }

    @Test
    void characteristicDefiningPowerAndToughnessCountOnTheStack() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int opponentLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new BeastOfBurden(), "{6}");
        awaitModeChoice();
        harness.handleListChoice(player1, DAMAGE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
    }

    @Test
    void entryAndCastTriggersShareChosenModes() {
        harness.castFromHand(player1, new TheFantasticFour(), "{R}{G}{W}{U}");
        awaitModeChoice();
        harness.handleListChoice(player1, WALL);
        resolveAllTriggers();

        harness.castFromHand(player1, new LukeCageHeroForHire(), "{2}{R}{R}");
        awaitModeChoice();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain(WALL);
        harness.handleListChoice(player1, DAMAGE);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Wall")).isEqualTo(1);
    }

    @Test
    void opponentQualifyingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new LukeCageHeroForHire(), "{2}{R}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Luke Cage, Hero for Hire");
    }

    @Test
    void counterModeCanTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new TheFantasticFour());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new LukeCageHeroForHire(), "{2}{R}{R}");
        awaitModeChoice();
        harness.handleListChoice(player1, COUNTERS);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
