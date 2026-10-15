package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CryogenRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NebulaDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TannukSteadfastSecond.class, GrizzlyBears.class, CryogenRelic.class, NebulaDragon.class})
class TannukSteadfastSecondTest extends BaseCardTest {

    @Test
    void otherCreaturesYouControlHaveHaste() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.addToBattlefield(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void warpsArtifactFromHand() {
        CryogenRelic relic = new CryogenRelic();
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(relic));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(relic.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(relic.getId())).isNotNull();
    }

    @Test
    void warpsRedCreatureFromHand() {
        NebulaDragon dragon = new NebulaDragon();
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(dragon.getId()));
        harness.assertLife(player2, 17);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(dragon.getId())).isNotNull();
    }

    @Test
    void doesNotGiveItselfHaste() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGiveOpponentsCreaturesHaste() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.addToBattlefield(player2, new NebulaDragon());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantWarpToNonredNonartifactCreature() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantWarpToOpponentsCards() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player2, List.of(new CryogenRelic()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseGrantedWarpWhenNormalCostIsAlsoAffordable() {
        CryogenRelic relic = new CryogenRelic();
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(relic));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Cryogen Relic");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(relic.getId())).isNotNull();
    }

    @Test
    void normallyCastArtifactIsNotExiled() {
        CryogenRelic relic = new CryogenRelic();
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(relic));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cryogen Relic");
        assertThat(gd.findExiledCard(relic.getId())).isNull();
    }

    @Test
    void warpedArtifactCanBeCastOnALaterTurnWithoutWarpingAgain() {
        CryogenRelic relic = new CryogenRelic();
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(relic));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gd.findExiledCard(relic.getId())).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, relic.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Cryogen Relic");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Cryogen Relic");
        assertThat(gd.findExiledCard(relic.getId())).isNull();
    }

    @Test
    void warpedRedCreatureCanAttackImmediately() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(new NebulaDragon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 13);
    }

    @Test
    void grantedWarpStillRequiresRedMana() {
        harness.addToBattlefield(player1, new TannukSteadfastSecond());
        harness.setHand(player1, List.of(new CryogenRelic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
