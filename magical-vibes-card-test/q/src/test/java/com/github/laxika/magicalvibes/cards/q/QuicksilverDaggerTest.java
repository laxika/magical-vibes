package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverDagger.class, GaeasSkyfolk.class, ChandraNalaar.class})
class QuicksilverDaggerTest extends BaseCardTest {

    @Test
    void enchantedCreatureDealsDamageAndControllerDraws() {
        Permanent creature = addReadyCreature();
        addAura(creature);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void enchantedCreatureCannotUseAbilityWhileSummoningSick() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());
        addAura(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    void abilityIsRemovedWhenAuraLeavesBattlefield() {
        Permanent creature = addReadyCreature();
        Permanent aura = addAura(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void auraCanBeCastAndAttachedToAValidCreature() {
        Permanent creature = addReadyCreature();
        harness.setHand(player1, List.of(new QuicksilverDagger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Quicksilver Dagger");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void enchantedCreatureDealsDamageToPlaneswalkerAndControllerDraws() {
        Permanent creature = addReadyCreature();
        addAura(creature);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void auraCannotBeCastTargetingAPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.setHand(player1, List.of(new QuicksilverDagger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, planeswalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsEnchantedCreatureDrawsForItsController() {
        Permanent creature = addCreatureReady(player2, new GaeasSkyfolk());
        addAura(creature);
        harness.setLibrary(player2, List.of(new GaeasSkyfolk()));
        int auraControllerHandSize = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandSize);
    }

    @Test
    void abilityCanTargetItsController() {
        Permanent creature = addReadyCreature();
        addAura(creature);
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void abilityCannotTargetACreature() {
        Permanent creature = addReadyCreature();
        addAura(creature);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityStillResolvesAfterAuraLeaves() {
        Permanent creature = addReadyCreature();
        Permanent aura = addAura(creature);
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void activatedAbilityStillResolvesAfterCreatureLeaves() {
        Permanent creature = addReadyCreature();
        addAura(creature);
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void noCardIsDrawnWhenTheOnlyTargetLeavesBeforeResolution() {
        Permanent creature = addReadyCreature();
        addAura(creature);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new GaeasSkyfolk());
    }

    private Permanent addAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new QuicksilverDagger());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
