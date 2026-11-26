package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.d.DourPortMage;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExileTargetPermanentEffect#all} resolved through the engine: every matching permanent of every battlefield is
 * exiled as one event, and "the number of creatures exiled this way" reads the exile step.
 */
@CardUsed({GrizzlyBears.class, HillGiant.class, GloriousAnthem.class, DourPortMage.class})
class ExileTargetPermanentEffectTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    private StackEntry resolve(Player controller, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(new GrizzlyBears(), controller.getId(), "Exile",
                List.of(effects), null, (UUID) null);
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    @Test
    @DisplayName("\"Exile all creatures. You gain 1 life for each creature exiled this way\" exiles every creature only")
    void exilesEveryMatchingPermanentAndCountsThem() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GloriousAnthem());
        int before = gd.getLife(player1.getId());

        resolve(player1, ExileTargetPermanentEffect.all(new PermanentIsCreaturePredicate()),
                new GainLifeEffect(new AmountThisWay(StepKind.EXILE)));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getLife(player1.getId())).isEqualTo(before + 2);
    }

    @Test
    @DisplayName("Creatures exiled together leave as one event: \"one or more other creatures leave\" triggers once")
    void setLeavesAsOneEvent() {
        addCreatureReady(player1, new DourPortMage());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolve(player1, ExileTargetPermanentEffect.all(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentHasSubtypePredicate(CardSubtype.BEAR)))));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
