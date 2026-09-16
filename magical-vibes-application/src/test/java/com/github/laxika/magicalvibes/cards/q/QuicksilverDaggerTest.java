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

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new GaeasSkyfolk());
    }

    private Permanent addAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new QuicksilverDagger());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
