package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrighthearthBanneret;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeethingPathblazer.class, BrighthearthBanneret.class, PricklyBoggart.class})
class SeethingPathblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Elemental gives +2/+0 and first strike until end of turn")
    void sacrificeElementalBoostsAndGrantsFirstStrike() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        Permanent elemental = addCreatureReady(player1, new BrighthearthBanneret());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(4); // 2 base + 2
        assertThat(gqs.getEffectiveToughness(gd, pathblazer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isTrue();

        // The Brighthearth Banneret was sacrificed
        harness.assertInGraveyard(player1, "Brighthearth Banneret");
    }

    @Test
    @DisplayName("Only Elementals are valid sacrifice choices")
    void onlyElementalsCanBeSacrificed() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        Permanent elemental = addCreatureReady(player1, new BrighthearthBanneret());
        Permanent nonElemental = addCreatureReady(player1, new PricklyBoggart());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(pathblazer.getId(), elemental.getId());
        assertThat(choice.validIds()).doesNotContain(nonElemental.getId());
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        Permanent elemental = addCreatureReady(player1, new BrighthearthBanneret());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can sacrifice itself to its own ability (only Elemental available)")
    void canSacrificeItself() {
        addCreatureReady(player1, new SeethingPathblazer());

        harness.activateAbility(player1, 0, null, null);

        // The ability still resolves, but its source is no longer on the battlefield.
        harness.assertNotOnBattlefield(player1, "Seething Pathblazer");
        harness.assertInGraveyard(player1, "Seething Pathblazer");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, while the boost waits for resolution")
    void sacrificeIsPaidBeforeResolution() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        Permanent elemental = addCreatureReady(player1, new BrighthearthBanneret());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elemental.getId());

        harness.assertInGraveyard(player1, "Brighthearth Banneret");
        harness.assertNotOnBattlefield(player1, "Brighthearth Banneret");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Repeated activations stack their power bonuses")
    void repeatedActivationsStack() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        Permanent first = addCreatureReady(player1, new BrighthearthBanneret());
        Permanent second = addCreatureReady(player1, new BrighthearthBanneret());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, pathblazer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures can activate and pay the sacrifice cost")
    void tappedAndSummoningSickCreaturesAreAllowed() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        Permanent elemental = addCreatureReady(player1, new BrighthearthBanneret());
        pathblazer.tap();
        pathblazer.setSummoningSick(true);
        elemental.tap();
        elemental.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brighthearth Banneret");
        assertThat(gqs.getEffectivePower(gd, pathblazer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, pathblazer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Elemental cannot pay the sacrifice cost")
    void opponentsElementalIsNotASacrificeChoice() {
        Permanent pathblazer = addCreatureReady(player1, new SeethingPathblazer());
        Permanent elemental = addCreatureReady(player1, new BrighthearthBanneret());
        Permanent opposingElemental = addCreatureReady(player2, new BrighthearthBanneret());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(pathblazer.getId(), elemental.getId());
        assertThat(choice.validIds()).doesNotContain(opposingElemental.getId());
    }
}
