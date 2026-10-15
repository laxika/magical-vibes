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

@CardUsed({SanguineStatuette.class, GrizzlyBears.class, SculptingSteel.class})
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

    @Test
    @DisplayName("A non-token copy of Blood does not trigger the animation")
    void sacrificingNontokenBloodDoesNotTrigger() {
        Permanent statuette = castStatuette();
        Permanent blood = findPermanent(player1, "Blood");
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());

        Permanent copiedBlood = findPermanents(player1, "Blood").stream()
                .filter(permanent -> !permanent.getId().equals(blood.getId()))
                .findFirst().orElseThrow();
        assertThat(copiedBlood.getCard().isToken()).isFalse();

        sacrificeBloodToken(copiedBlood);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, statuette)).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getSourcePermanentId() != null
                && entry.getSourcePermanentId().equals(statuette.getId()));
    }

    @Test
    @DisplayName("An animated Statuette can attack on the turn it enters")
    void hasteAllowsAttackingImmediately() {
        Permanent statuette = castStatuette();
        sacrificeBloodToken(findPermanent(player1, "Blood"));
        harness.handleMayAbilityChosen(player1, true);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(statuette)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent castStatuette() {
        Card card = new SanguineStatuette();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

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
