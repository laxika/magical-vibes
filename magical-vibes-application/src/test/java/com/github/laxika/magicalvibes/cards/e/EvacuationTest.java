package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CloudSpirit;
import com.github.laxika.magicalvibes.cards.p.Portcullis;
import com.github.laxika.magicalvibes.cards.r.ReinsOfPower;
import com.github.laxika.magicalvibes.cards.s.SliverQueen;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Evacuation.class, CloudSpirit.class, SpinedWurm.class, Portcullis.class,
        VolrathsStronghold.class, ReinsOfPower.class, SliverQueen.class})
class EvacuationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Returns all creatures on both sides to their owners' hands")
    void returnsAllCreaturesToHands() {
        harness.addToBattlefield(player1, new CloudSpirit());
        harness.addToBattlefield(player1, new SpinedWurm());
        harness.addToBattlefield(player2, new CloudSpirit());

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactlyInAnyOrder(CloudSpirit.class, SpinedWurm.class);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> (Object) card.getClass())
                .contains(CloudSpirit.class);
    }

    @Test
    @DisplayName("Does not return non-creature permanents")
    void doesNotReturnNonCreaturePermanents() {
        harness.addToBattlefield(player1, new Portcullis());
        harness.addToBattlefield(player1, new VolrathsStronghold());
        harness.addToBattlefield(player1, new CloudSpirit());

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> (Object) permanent.getCard().getClass())
                .containsExactlyInAnyOrder(Portcullis.class, VolrathsStronghold.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(CloudSpirit.class);
    }

    @Test
    @DisplayName("Returns creature tokens, which cease to exist outside the battlefield")
    void returnsCreatureTokens() {
        harness.addToBattlefield(player1, new SliverQueen());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(SliverQueen.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.isToken());
    }

    @Test
    @DisplayName("Works with empty battlefields (no crash)")
    void worksWithEmptyBattlefield() {
        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evacuation goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .contains(Evacuation.class);
    }

    @Test
    @DisplayName("Creatures return to their owners' hands after a control exchange")
    void creaturesReturnToTheirOwnersHandsAfterControlExchange() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CloudSpirit());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());

        harness.setHand(player1, List.of(new ReinsOfPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(CloudSpirit.class);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> (Object) card.getClass())
                .contains(SpinedWurm.class);
    }
}

