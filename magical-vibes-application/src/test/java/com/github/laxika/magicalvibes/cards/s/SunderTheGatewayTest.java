package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RealmbreakerTheInvasionTree;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunderTheGateway.class, SculptedPerfection.class, RealmbreakerTheInvasionTree.class, SongOfTheDryads.class})
class SunderTheGatewayTest extends BaseCardTest {

    @Test
    void destroysOpponentEnchantmentAndIncubates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SculptedPerfection());
        cast(player1, List.of(new SunderTheGateway()), target.getId(), 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gameData.playerGraveyards.get(player2.getId())).contains(target.getCard());

        Permanent incubator = findIncubator(player1);
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void secondModeIncubatesThenTransformsChosenToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SculptedPerfection());
        harness.setHand(player1, List.of(new SunderTheGateway(), new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        Permanent incubator = findIncubator(player1);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(2);
    }

    @Test
    void firstModeCannotTargetPermanentYouControl() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new SculptedPerfection());
        harness.addToBattlefield(player2, new SculptedPerfection());
        harness.setHand(player1, List.of(new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysOpponentArtifactAndIncubates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RealmbreakerTheInvasionTree());
        cast(player1, List.of(new SunderTheGateway()), target.getId(), 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(findIncubator(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void secondModeTransformsNewTokenWhenNoIncubatorAlreadyExists() {
        harness.setHand(player1, List.of(new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, 1);

        Permanent token = findIncubator(player1);
        assertThat(token.isTransformed()).isTrue();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void firstModeCannotTargetOpponentToken() {
        harness.enterBattlefieldAndReturn(player2, new SculptedPerfection());
        harness.passBothPriorities();
        Permanent token = findIncubator(player2);

        harness.setHand(player1, List.of(new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalTargetAtResolutionPreventsIncubation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SculptedPerfection());
        harness.setHand(player1, List.of(new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void firstModeTokenCanTransformUsingItsActivatedAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SculptedPerfection());
        cast(player1, List.of(new SunderTheGateway()), target.getId(), 0);
        Permanent token = findIncubator(player1);
        assertThat(token.isTransformed()).isFalse();
        assertThat(gqs.isCreature(gd, token)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(token.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void incubatorTransformedIntoForestIsNotEligibleForSecondMode() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SculptedPerfection());
        cast(player1, List.of(new SunderTheGateway()), target.getId(), 0);
        Permanent oldToken = findIncubator(player1);

        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, oldToken.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, oldToken,
                com.github.laxika.magicalvibes.model.CardSubtype.INCUBATOR)).isFalse();

        harness.setHand(player1, List.of(new SunderTheGateway()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(oldToken.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken() && permanent != oldToken))
                .singleElement().satisfies(token -> assertThat(token.isTransformed()).isTrue());
    }

    private void cast(com.github.laxika.magicalvibes.model.Player player, List<com.github.laxika.magicalvibes.model.Card> cards,
                      java.util.UUID targetId, int mode) {
        harness.setHand(player, cards);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player, 0, mode, targetId);
    }

    private Permanent findIncubator(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElse(null);
    }
}
