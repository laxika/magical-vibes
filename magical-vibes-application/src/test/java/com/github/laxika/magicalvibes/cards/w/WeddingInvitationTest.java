package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeddingInvitation.class, FountainOfYouth.class, GrizzlyBears.class, VampireNoble.class})
class WeddingInvitationTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsACardWhenItEnters() {
        harness.setHand(player1, List.of(new WeddingInvitation()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wedding Invitation");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Makes a target creature unblockable until end of turn")
    void makesTargetCreatureUnblockable() {
        Permanent invitation = harness.addToBattlefieldAndReturn(player1, new WeddingInvitation());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wedding Invitation");
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, target)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(invitation);
    }

    @Test
    @DisplayName("Also grants lifelink when the target is a Vampire")
    void grantsLifelinkToVampire() {
        harness.addToBattlefield(player1, new WeddingInvitation());
        Permanent vampire = addCreatureReady(player2, new VampireNoble());

        harness.activateAbility(player1, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, vampire)).isTrue();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WeddingInvitation());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
