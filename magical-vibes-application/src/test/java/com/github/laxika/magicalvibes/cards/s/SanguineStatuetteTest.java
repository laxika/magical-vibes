package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineStatuette.class, GrizzlyBears.class})
class SanguineStatuetteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Blood token")
    void entersWithBloodToken() {
        castStatuette();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Blood token may animate Sanguine Statuette")
    void sacrificingBloodMayAnimateStatuette() {
        Permanent statuette = castStatuette();
        Permanent blood = findPermanent(player1, "Blood");

        sacrificeBloodToken(blood);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isCreature(gd, statuette)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statuette)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, statuette)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, statuette)).containsExactly(CardSubtype.VAMPIRE);
        assertThat(gqs.hasKeyword(gd, statuette, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the animation leaves Sanguine Statuette unchanged")
    void decliningAnimationDoesNothing() {
        Permanent statuette = castStatuette();
        Permanent blood = findPermanent(player1, "Blood");

        sacrificeBloodToken(blood);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isCreature(gd, statuette)).isFalse();
        assertThat(gqs.hasKeyword(gd, statuette, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent statuette = castStatuette();
        Permanent blood = findPermanent(player1, "Blood");

        sacrificeBloodToken(blood);
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statuette)).isFalse();
        assertThat(gqs.hasKeyword(gd, statuette, Keyword.HASTE)).isFalse();
    }

    private Permanent castStatuette() {
        Card card = new SanguineStatuette();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Sanguine Statuette");
    }

    private void sacrificeBloodToken(Permanent blood) {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
