package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.SenateGriffin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepudiateReplicate.class, RodOfRuin.class, GrizzlyBears.class,
        SauroformHybrid.class, SenateGriffin.class})
class RepudiateReplicateTest extends BaseCardTest {

    private static final int REPUDIATE = 0;
    private static final int REPLICATE = 1;

    @Test
    @DisplayName("Repudiate counters an activated ability")
    void repudiateCountersActivatedAbility() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RepudiateReplicate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, REPUDIATE, rod.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Repudiate cannot target a spell")
    void repudiateCannotTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RepudiateReplicate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, REPUDIATE, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Replicate creates a token copy of a creature you control")
    void replicateCreatesTokenCopy() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, REPLICATE, bears.getId());

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Replicate cannot target an opponent's creature")
    void replicateCannotTargetOpponentCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, REPLICATE, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repudiateCountersTriggeredAbilityWithoutRemovingItsSource() {
        harness.setLibrary(player2, List.of(new SauroformHybrid()));
        Permanent griffin = harness.enterBattlefieldAndReturn(player2, new SenateGriffin());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, REPUDIATE, abilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(griffin);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void repudiateCanCounterYourOwnActivatedAbilityWithMixedHybridMana() {
        Permanent hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, REPUDIATE, abilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hybrid);
    }

    @Test
    void replicateCannotBeCastDuringOpponentsTurn() {
        Permanent hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        addReplicateMana();
        harness.forceActivePlayer(player2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REPLICATE, hybrid.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void replicateCannotBeCastInResponseToAnAbility() {
        Permanent hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        addReplicateMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REPLICATE, hybrid.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void replicateDoesNotCopyCountersOrTappedState() {
        Permanent hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        hybrid.tap();
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        addReplicateMana();

        harness.castAndResolveSorcery(player1, 0, REPLICATE, hybrid.getId());

        assertThat(findPermanents(player1, "Sauroform Hybrid")).hasSize(2);
        Permanent token = findPermanents(player1, "Sauroform Hybrid").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(hybrid.isTapped()).isTrue();
    }

    @Test
    void replicateTokenTriggersCopiedEnterBattlefieldAbility() {
        harness.addToBattlefield(player1, new SenateGriffin());
        harness.setLibrary(player1, List.of(new SauroformHybrid()));
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        addReplicateMana();

        harness.castAndResolveSorcery(player1, 0, REPLICATE,
                findPermanent(player1, "Senate Griffin").getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Senate Griffin")).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void replicateDoesNotCreateTokenWhenTargetLeavesBattlefield() {
        Permanent hybrid = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        harness.setHand(player1, List.of(new RepudiateReplicate()));
        addReplicateMana();
        harness.castSorcery(player1, 0, REPLICATE, hybrid.getId());
        gd.playerBattlefields.get(player1.getId()).remove(hybrid);
        harness.setGraveyard(player1, List.of(hybrid.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Repudiate // Replicate");
    }

    private void addReplicateMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
