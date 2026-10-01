package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.s.StaffOfNin;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbstruseArchaic.class, Arena.class, Forest.class, IchorWellspring.class,
        StaffOfNin.class, TrialOfZeal.class})
class AbstruseArchaicTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a triggered ability from a colorless source")
    void copiesColorlessTriggeredAbility() {
        addCreatureReady(player1, new AbstruseArchaic());
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        UUID triggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        int handBeforeResolution = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, triggerId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeResolution + 2);
    }

    @Test
    @DisplayName("Cannot target an ability from a colored source")
    void cannotTargetColoredSourceAbility() {
        addCreatureReady(player1, new AbstruseArchaic());
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        UUID triggerId = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, triggerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies a colorless activated ability and may choose a new target")
    void copiesActivatedAbilityWithNewTarget() {
        Permanent archaic = addCreatureReady(player1, new AbstruseArchaic());
        harness.addToBattlefield(player1, new StaffOfNin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, null, abilityId);
        assertThat(archaic.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining new targets keeps the original activated ability target")
    void copiesActivatedAbilityKeepingTarget() {
        addCreatureReady(player1, new AbstruseArchaic());
        harness.addToBattlefield(player1, new StaffOfNin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.activateAbility(player1, 0, null, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot copy an opponent's colorless activated ability")
    void cannotCopyOpponentAbility() {
        addCreatureReady(player1, new AbstruseArchaic());
        harness.addToBattlefield(player2, new StaffOfNin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, player1.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Vigilance leaves Abstruse Archaic untapped after attacking")
    void remainsUntappedAfterAttacking() {
        Permanent archaic = addCreatureReady(player1, new AbstruseArchaic());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(archaic.isTapped()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot copy a colorless spell on the stack")
    void cannotCopySpell() {
        addCreatureReady(player1, new AbstruseArchaic());
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        UUID spellId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spellId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap activation cannot be used while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new AbstruseArchaic());
        harness.addToBattlefield(player1, new StaffOfNin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May choose new targets when copying an ability with multiple targets")
    void offersNewTargetsForMultiTargetAbility() {
        Permanent archaic = addCreatureReady(player1, new AbstruseArchaic());
        harness.addToBattlefield(player1, new Arena());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AbstruseArchaic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, archaic.getId());
        harness.handlePermanentChosen(player2, opponentCreature.getId());
        harness.activateAbility(player1, 0, null, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
    }
}
