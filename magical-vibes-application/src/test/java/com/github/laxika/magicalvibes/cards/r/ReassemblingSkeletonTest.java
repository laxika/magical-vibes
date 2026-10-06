package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Reassembling Skeleton")
@CardUsed({ReassemblingSkeleton.class, LightningBolt.class})
class ReassemblingSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating graveyard ability puts it on the stack")
    void activatingGraveyardAbilityPutsOnStack() {
        ReassemblingSkeleton skeleton = new ReassemblingSkeleton();
        harness.setGraveyard(player1, List.of(skeleton));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reassembling Skeleton");
    }

    @Test
    @DisplayName("Resolving graveyard ability returns Reassembling Skeleton to the battlefield tapped")
    void resolvingGraveyardAbilityReturnsToBattlefieldTapped() {
        ReassemblingSkeleton skeleton = new ReassemblingSkeleton();
        harness.setGraveyard(player1, List.of(skeleton));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        // Should be on the battlefield, tapped
        Permanent perm = findPermanent(player1, "Reassembling Skeleton");
        assertThat(perm.isTapped()).isTrue();

        // Should no longer be in graveyard
        harness.assertNotInGraveyard(player1, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("Cannot activate graveyard ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        ReassemblingSkeleton skeleton = new ReassemblingSkeleton();
        harness.setGraveyard(player1, List.of(skeleton));
        harness.addMana(player1, ManaColor.BLACK, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard ability pays mana cost")
    void graveyardAbilityPaysManaCost() {
        ReassemblingSkeleton skeleton = new ReassemblingSkeleton();
        harness.setGraveyard(player1, List.of(skeleton));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate graveyard ability again after being returned and dying again")
    void canActivateGraveyardAbilityMultipleTimes() {
        ReassemblingSkeleton skeleton = new ReassemblingSkeleton();
        harness.setGraveyard(player1, List.of(skeleton));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // First activation: return to battlefield
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");

        // Simulate dying: remove from battlefield and put back in graveyard
        Permanent perm = findPermanent(player1, "Reassembling Skeleton");
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(perm.getCard());

        // Second activation: return to battlefield again
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("Only the activating Skeleton returns, even with other copies in both graveyards")
    void returnsOnlyTheActivatingCopy() {
        ReassemblingSkeleton first = new ReassemblingSkeleton();
        ReassemblingSkeleton second = new ReassemblingSkeleton();
        ReassemblingSkeleton opposing = new ReassemblingSkeleton();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opposing));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Reassembling Skeleton").getCard()).isSameAs(second);
        assertThat(findPermanent(player1, "Reassembling Skeleton").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
        harness.assertNotOnBattlefield(player2, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("Two activations return the Skeleton only once")
    void twoActivationsReturnOnlyOnce() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Reassembling Skeleton")).isEqualTo(1);
        assertThat(findPermanent(player1, "Reassembling Skeleton").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Reassembling Skeleton");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An older activation cannot return the Skeleton after it returns and dies again")
    void olderActivationCannotReturnANewGraveyardObject() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Reassembling Skeleton"));
        harness.assertInGraveyard(player1, "Reassembling Skeleton");
        harness.assertNotOnBattlefield(player1, "Reassembling Skeleton");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reassembling Skeleton");
        harness.assertNotOnBattlefield(player1, "Reassembling Skeleton");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate during the opponent's end step")
    void canActivateDuringOpponentsEndStep() {
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Reassembling Skeleton").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Reassembling Skeleton");
    }
}
