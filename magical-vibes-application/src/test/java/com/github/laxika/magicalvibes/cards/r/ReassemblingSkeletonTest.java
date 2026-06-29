package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Reassembling Skeleton")
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
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Reassembling Skeleton"));
        Permanent perm = bf.stream()
                .filter(p -> p.getCard().getName().equals("Reassembling Skeleton"))
                .findFirst().orElseThrow();
        assertThat(perm.isTapped()).isTrue();

        // Should no longer be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Reassembling Skeleton"));
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reassembling Skeleton"));

        // Simulate dying: remove from battlefield and put back in graveyard
        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Reassembling Skeleton"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(perm.getCard());

        // Second activation: return to battlefield again
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reassembling Skeleton"));
    }
}
