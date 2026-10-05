package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DisruptionProtocol;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LionSash.class, Plains.class, DisruptionProtocol.class, BambooGroveArcher.class, EnchantedEvening.class})
class LionSashTest extends BaseCardTest {

    @Test
    void exilingPermanentCardPutsCounterOnLionSash() {
        Permanent sash = addSash();
        Card land = new Plains();
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateExileAbility(sash, land);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void exilingNonpermanentCardDoesNotPutCounterOnLionSash() {
        Permanent sash = addSash();
        Card instant = new DisruptionProtocol();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateExileAbility(sash, instant);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(instant);
        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void equippedCreatureGetsPlusOneForEachCounterOnLionSash() {
        Permanent sash = addSash();
        sash.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        sash.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
    }

    @Test
    void reconfigureAttachesAndUnattachesLionSash() {
        Permanent sash = addSash();
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, host.getId());
        harness.passBothPriorities();

        assertThat(sash.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, sash)).isFalse();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(sash.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, sash)).isTrue();
    }

    @Test
    void reconfigureCannotTargetOpponentsCreature() {
        Permanent sash = addSash();
        Permanent opponentCreature = addCreatureReady(player2, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sash.getAttachedTo()).isNull();
    }

    @Test
    void unavailableGraveyardTargetDoesNotGiveCounter() {
        Permanent sash = addSash();
        Card land = new Plains();
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.WHITE, 1);
        activateExileAbility(sash, land);
        harness.setGraveyard(player2, List.of());

        harness.passBothPriorities();

        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(land);
    }

    @Test
    void exileStillResolvesAfterLionSashLeavesBattlefield() {
        Permanent sash = addSash();
        Card land = new Plains();
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.WHITE, 1);
        activateExileAbility(sash, land);
        gd.playerBattlefields.get(player1.getId()).remove(sash);
        harness.setGraveyard(player1, List.of(sash.getCard()));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void exilingPermanentWhileEquippedImmediatelyIncreasesHostBonus() {
        Permanent sash = addSash();
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, host.getId());
        harness.passBothPriorities();
        Card land = new Plains();
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateExileAbility(sash, land);
        harness.passBothPriorities();

        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, sash)).isFalse();
    }

    @Test
    void reconfigureCannotTargetLionSashItself() {
        Permanent sash = addSash();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, sash.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unattachCannotBeActivatedWhileUnattached() {
        addSash();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothReconfigureModesRequireSorceryTiming() {
        Permanent sash = addSash();
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, host.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sash.getAttachedTo()).isEqualTo(host.getId());
    }

    @Test
    @CardUsed(EnchantedEvening.class)
    void reconfigurePreservesOtherCardTypes() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent sash = addSash();
        Permanent host = addCreatureReady(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 1, null, host.getId());
        harness.passBothPriorities();

        assertThat(sash.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, sash)).isFalse();
        assertThat(gqs.isEnchantment(gd, sash)).isTrue();
    }

    @Test
    void reconfigureMovesBetweenCreaturesAndUnattachRemovesBonus() {
        Permanent sash = addSash();
        sash.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent first = addCreatureReady(player1, new BambooGroveArcher());
        Permanent second = addCreatureReady(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, first.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(sash.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, sash)).isTrue();
        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sash)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sash)).isEqualTo(3);
    }

    @Test
    void exileAbilityWorksWhileSummoningSickOutsideMainPhase() {
        Permanent sash = harness.addToBattlefieldAndReturn(player1, new LionSash());
        sash.setSummoningSick(true);
        Card creature = new BambooGroveArcher();
        harness.setGraveyard(player2, List.of(creature));
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateExileAbility(sash, creature);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    private void activateExileAbility(Permanent sash, Card target) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(sash);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
    }

    private Permanent addSash() {
        return addCreatureReady(player1, new LionSash());
    }
}
