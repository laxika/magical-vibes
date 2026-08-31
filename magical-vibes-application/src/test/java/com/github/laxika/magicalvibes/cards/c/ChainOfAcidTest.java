package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainOfAcid.class, Forest.class, GrizzlyBears.class})
class ChainOfAcidTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target noncreature permanent and asks its controller to copy the spell")
    void destroysNoncreaturePermanentAndOffersCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAt(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The destroyed permanent's controller may create a copy")
    void targetControllerMayCopySpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining the copy creates no copy")
    void decliningCopyCreatesNoCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChainOfAcid()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ChainOfAcid()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
