package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.t.TheloniteHermit;
import com.github.laxika.magicalvibes.cards.s.Sprout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveChoice.class, GrizzlyBears.class, HillGiant.class, Unsummon.class,
        TheloniteHermit.class, Sprout.class})
class GraveChoiceTest extends BaseCardTest {

    @Test
    void sacrificesOpponentCreatureAndConjuresSmallDuplicateWithAnyColorCasting() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGraveChoice();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(bears.getCard().getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(duplicate));
    }

    @Test
    void doesNotConjureDuplicateWhenSacrificedCreatureIsTooExpensive() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castGraveChoice();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(giant.getCard());
    }

    @Test
    void targetedOpponentChoosesTheCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castGraveChoice();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());

        harness.handlePermanentChosen(player2, giant.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castGraveChoice() {
        harness.setHand(player1, List.of(new GraveChoice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    @Test
    void resolvesWithoutConjuringWhenOpponentHasNoCreatures() {
        castGraveChoice();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grave Choice");
    }

    @Test
    void leavesTokensAloneAndSacrificesOnlyTheNontokenCreature() {
        Permanent token = createOpponentToken();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGraveChoice();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(token);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotSacrificeOrConjureWhenOpponentControlsOnlyTokens() {
        Permanent token = createOpponentToken();

        castGraveChoice();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(token);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetTheCaster() {
        harness.setHand(player1, List.of(new GraveChoice()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conjuresDuplicateWhenSacrificedFaceDownCreatureHasExpensiveFrontFace() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TheloniteHermit()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        Permanent faceDownCreature = gd.playerBattlefields.get(player2.getId()).getFirst();

        castGraveChoice();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(faceDownCreature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void duplicateKeepsAnyColorCastingAfterReturningToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castGraveChoice();
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent conjuredCreature = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, conjuredCreature.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getId()).isEqualTo(duplicate.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(duplicate.getId()));
    }

    private Permanent createOpponentToken() {
        harness.setHand(player2, List.of(new Sprout()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0);
        return gd.playerBattlefields.get(player2.getId()).getFirst();
    }
}
