package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OverloadedMageRing;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CopperMyr.class, Forest.class, InvasionOfVryn.class, Island.class,
        Mountain.class, OverloadedMageRing.class, Shock.class})
class InvasionOfVrynTest extends BaseCardTest {

    @Test
    void entersDrawsThreeThenRequiresDiscardingOne() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new InvasionOfVryn(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void defeatCastsOverloadedMageRingTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfVryn());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent ring = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof OverloadedMageRing)
                .findFirst()
                .orElseThrow();
        assertThat(ring.isTransformed()).isTrue();
    }

    @Test
    void copiesPermanentSpellAsToken() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new InvasionOfVryn());
        ring.setCard(new OverloadedMageRing());
        ring.setTransformed(true);

        CopperMyr myr = new CopperMyr();
        harness.setHand(player1, List.of(myr));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, 0, null, myr.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> "Copper Myr".equals(permanent.getCard().getName()))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void copiesInstantAndOffersNewTargetChoice() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new InvasionOfVryn());
        ring.setCard(new OverloadedMageRing());
        ring.setTransformed(true);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, shock.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void cannotCopySpellControlledByOpponent() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new InvasionOfVryn());
        ring.setCard(new OverloadedMageRing());
        ring.setTransformed(true);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayDeclineCastingDefeatedBattle() {
        InvasionOfVryn card = new InvasionOfVryn();
        Permanent battle = harness.addToBattlefieldAndReturn(player1, card);
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void copyingTransformedSpellPreservesBackFace() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new InvasionOfVryn());
        ring.setCard(new OverloadedMageRing());
        ring.setTransformed(true);
        InvasionOfVryn card = new InvasionOfVryn();
        Permanent battle = harness.addToBattlefieldAndReturn(player1, card);
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getCard().getName()).isEqualTo("Overloaded Mage-Ring");
                });
    }

    @Test
    void copiedInstantCanDamageDifferentTarget() {
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new InvasionOfVryn());
        ring.setCard(new OverloadedMageRing());
        ring.setTransformed(true);
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, 0, null, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, myr.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Copper Myr");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Overloaded Mage-Ring");
    }
}
