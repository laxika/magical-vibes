package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VirtuousVariant.class, GrizzlyBears.class})
class VirtuousVariantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a target creature you control")
    void etbPutsCounterOnTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVariant(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VirtuousVariant()));
        addVariantMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castVariant(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new VirtuousVariant()));
        addVariantMana();
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("On an empty battlefield the entry trigger can target Variant itself")
    void canPutCounterOnItself() {
        harness.castFromHand(player1, new VirtuousVariant(), "{2}{W}");
        harness.passBothPriorities();
        Permanent variant = findPermanent(player1, "Virtuous Variant");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, variant.getId());
        resolveAllTriggers();

        assertThat(variant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entry trigger does not put a counter on a target now controlled by the opponent")
    void targetChangingControllerBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VirtuousVariant());
        harness.setHand(player1, List.of(new VirtuousVariant()));
        addVariantMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entry trigger resolves after Variant leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VirtuousVariant());
        harness.setHand(player1, List.of(new VirtuousVariant()));
        addVariantMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void addVariantMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
