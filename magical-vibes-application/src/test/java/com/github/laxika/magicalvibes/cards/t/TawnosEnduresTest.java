package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TawnosEndures.class, GrizzlyBears.class, Forest.class})
class TawnosEnduresTest extends BaseCardTest {

    @Test
    void exilesCreatureAndReturnsItAtOwnersUpkeepWithPerpetualBoost() {
        GrizzlyBears targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        castTawnosEndures(target);

        assertThat(gd.findExiledCard(targetCard.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(targetCard.getId())).isNotNull();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
    }

    @Test
    void doesNothingIfTheCardLeavesExileBeforeItsUpkeepAbilityResolves() {
        GrizzlyBears targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        castTawnosEndures(target);

        advanceToUpkeep(player2);
        gd.removeFromExile(targetCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(targetCard.getId()));
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TawnosEndures()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castTawnosEndures(Permanent target) {
        harness.setHand(player1, List.of(new TawnosEndures()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
