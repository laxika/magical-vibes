package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HiddenStringsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped permanent and taps an untapped one")
    void togglesBothTargets() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        mine.tap();
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(mine.isTapped()).isFalse();
        assertThat(theirs.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Hidden Strings");
    }

    @Test
    @DisplayName("Cannot choose the same permanent for both targets")
    void cannotTargetSamePermanentTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encodes on a creature and casts a copy after combat damage")
    void encodesAndCastsCopy() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Hidden Strings"));
        harness.assertNotInGraveyard(player1, "Hidden Strings");
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }
}
