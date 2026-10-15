package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PantherPounce.class, GrizzlyBears.class, ErdwalIlluminator.class})
class PantherPounceTest extends BaseCardTest {

    @Test
    @DisplayName("Target player creates a Clue and target creature is untapped and boosted")
    void investigatesAndPounces() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new PantherPounce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(player2.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Target player's investigate triggers resolve")
    void triggersTargetPlayersInvestigateAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ErdwalIlluminator());
        harness.setHand(player1, List.of(new PantherPounce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(player2.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Caster can investigate while an opposing creature gets the pounce effects")
    void investigatesForCasterAndPouncesOnOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new PantherPounce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(player1.getId(), target.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Target player still investigates when the creature target leaves before resolution")
    void investigatesWhenCreatureTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PantherPounce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(player2.getId(), target.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "Panther Pounce");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
