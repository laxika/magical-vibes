package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RocketPoweredGoblinGlider.class, GrizzlyBears.class})
class RocketPoweredGoblinGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Mayhem casts the Glider from the graveyard and attaches it to a creature you control")
    void mayhemCastAttachesAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        RocketPoweredGoblinGlider glider = new RocketPoweredGoblinGlider();
        harness.setGraveyard(player1, List.of(glider));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(glider.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent resolvedGlider = findPermanent(player1, "Rocket-Powered Goblin Glider");
        assertThat(resolvedGlider.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A normal cast does not trigger the graveyard-cast attachment ability")
    void normalCastLeavesGliderUnattached() {
        harness.setHand(player1, List.of(new RocketPoweredGoblinGlider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent resolvedGlider = findPermanent(player1, "Rocket-Powered Goblin Glider");
        assertThat(resolvedGlider.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Mayhem cannot target an opponent's creature for the attachment")
    void mayhemCannotTargetOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        RocketPoweredGoblinGlider glider = new RocketPoweredGoblinGlider();
        harness.setGraveyard(player1, List.of(glider));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(glider.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipTransfersAllBonusesToTheNewCreature() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new RocketPoweredGoblinGlider());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(glider.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        assertThat(glider.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    void mayhemRequiresThisCardToHaveBeenDiscarded() {
        RocketPoweredGoblinGlider glider = new RocketPoweredGoblinGlider();
        RocketPoweredGoblinGlider otherGlider = new RocketPoweredGoblinGlider();
        harness.setGraveyard(player1, List.of(glider, otherGlider));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(otherGlider.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(glider);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayhemWithoutLegalAttachmentTargetStillResolves() {
        RocketPoweredGoblinGlider glider = new RocketPoweredGoblinGlider();
        harness.setGraveyard(player1, List.of(glider));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(glider.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rocket-Powered Goblin Glider").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayhemDoesNotAllowCastingOutsideMainPhase() {
        RocketPoweredGoblinGlider glider = new RocketPoweredGoblinGlider();
        harness.setGraveyard(player1, List.of(glider));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(glider.getId())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(glider);
    }
    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
