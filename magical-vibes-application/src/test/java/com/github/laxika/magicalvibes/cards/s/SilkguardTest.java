package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.z.ZephidsEmbrace;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Silkguard.class, GrizzlyBears.class, JoustingLance.class, ZephidsEmbrace.class})
class SilkguardTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one counter on each of up to X creatures and protects modified permanents")
    void putsCountersAndGrantsHexproofToModifiedPermanents() {
        Permanent alreadyModified = addCreatureReady(player1, new GrizzlyBears());
        alreadyModified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent notTargeted = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = new Permanent(new ZephidsEmbrace());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        aura.setAttachedTo(enchantedCreature.getId());

        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        equipment.setAttachedTo(firstTarget.getId());

        castSilkguard(2, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(notTargeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, alreadyModified, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstTarget, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondTarget, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipment, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, notTargeted, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Cannot target creatures an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForX(
                player1, 0, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSilkguard(int x, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new Silkguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);
        harness.castInstantForX(player1, 0, x, targetIds);
        harness.passBothPriorities();
    }
}
