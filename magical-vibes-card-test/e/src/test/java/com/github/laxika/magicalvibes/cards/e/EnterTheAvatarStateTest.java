package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({EnterTheAvatarState.class, GrizzlyBears.class, Plains.class, Unsummon.class})
class EnterTheAvatarStateTest extends BaseCardTest {

    @Test
    @DisplayName("Turns a creature you control into an Avatar with four keywords until end of turn")
    void grantsAvatarSubtypeAndKeywords() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(bears);

        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.BEAR, CardSubtype.AVATAR);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The subtype and keyword grants wear off at cleanup")
    void grantsWearOffAtCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.BEAR);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnterTheAvatarState()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new EnterTheAvatarState()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    void cannotTargetNoncreatureYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new EnterTheAvatarState()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyTargetCreatureReceivesGrants() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);

        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).containsExactly(CardSubtype.BEAR);
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK, Keyword.HEXPROOF)) {
            assertThat(gqs.hasKeyword(gd, other, keyword)).isFalse();
        }
    }

    @Test
    void hexproofPreventsOpponentTargetingButAllowsControllerTargeting() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(bears);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void targetLeavingBattlefieldInResponseMakesSpellDoNothing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnterTheAvatarState()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, bears.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Enter the Avatar State");
        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).containsExactly(CardSubtype.BEAR);
        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK, Keyword.HEXPROOF)) {
            assertThat(gqs.hasKeyword(gd, other, keyword)).isFalse();
        }
    }
}
