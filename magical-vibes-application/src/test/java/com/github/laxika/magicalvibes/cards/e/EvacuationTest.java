package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SiegeGangCommander;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Evacuation.class, AirElemental.class, SpinedWurm.class, GloriousAnthem.class,
        Island.class, SiegeGangCommander.class, Threaten.class, TrollAscetic.class, Pacifism.class})
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
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new SpinedWurm());
        harness.addToBattlefield(player2, new AirElemental());

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactlyInAnyOrder(AirElemental.class, SpinedWurm.class);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> (Object) card.getClass())
                .contains(AirElemental.class);
    }

    @Test
    @DisplayName("Does not return non-creature permanents")
    void doesNotReturnNonCreaturePermanents() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new AirElemental());

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> (Object) permanent.getCard().getClass())
                .containsExactlyInAnyOrder(GloriousAnthem.class, Island.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(AirElemental.class);
    }

    @Test
    @DisplayName("Returns creature tokens, which cease to exist outside the battlefield")
    void returnsCreatureTokens() {
        harness.castFromHand(player1, new SiegeGangCommander(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3);

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(SiegeGangCommander.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.isToken());
    }

    @Test
    @DisplayName("Returns opposing creatures with hexproof without targeting them")
    void returnsOpposingHexproofCreatures() {
        harness.addToBattlefield(player2, new TrollAscetic());

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(TrollAscetic.class);
    }

    @Test
    @DisplayName("Attached noncreature Auras go to the graveyard rather than returning to hand")
    void attachedAurasGoToGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pacifism");

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotInHand(player1, "Pacifism");
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SpinedWurm());

        harness.setHand(player2, List.of(new Threaten()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player2, 0, ownCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactlyInAnyOrder(ownCreature, opposingCreature);

        harness.castFromHand(player1, new Evacuation(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(AirElemental.class);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> (Object) card.getClass())
                .contains(SpinedWurm.class);
    }
}

