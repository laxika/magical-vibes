package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.Exploration;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenStag.class, Exploration.class, Forest.class, Clone.class, Rescind.class})
class HiddenStagTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's land play makes Hidden Stag a 3/2 Elk Beast creature")
    void becomesCreatureWhenOpponentPlaysLand() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenStag)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hiddenStag)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenStag))
                .containsExactlyInAnyOrder(CardSubtype.ELK, CardSubtype.BEAST);
    }

    @Test
    @DisplayName("A controller's land play restores Hidden Stag as an enchantment")
    void becomesEnchantmentWhenControllerPlaysLand() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();

        prepareLandPlay(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenStag)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenStag)).isFalse();
    }

    @Test
    @DisplayName("A controller's land play does not trigger Hidden Stag while it is an enchantment")
    void doesNotTriggerControllerLandPlayWhileEnchantment() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenStag)).isFalse();
    }

    @Test
    @DisplayName("An opponent's additional land play does not retrigger Hidden Stag while it is a creature")
    void doesNotTriggerOpponentLandPlayWhileCreature() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        harness.addToBattlefield(player2, new Exploration());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest(), new Forest()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();

        prepareLandPlay(player2);
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();
    }

    @Test
    @DisplayName("Hidden Stag can become a creature again after reverting to an enchantment")
    void canAnimateAgainAfterReverting() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        harness.addToBattlefield(player2, new Exploration());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        prepareLandPlay(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenStag)).isFalse();

        prepareLandPlay(player2);
        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenStag)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hiddenStag)).isEqualTo(2);
    }

    @Test
    @DisplayName("Putting lands onto the battlefield does not trigger either ability")
    void enteringLandsDoNotTriggerEitherAbility() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenStag)).isFalse();

        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenStag)).isFalse();
    }

    @Test
    @DisplayName("An animation trigger does nothing if Hidden Stag leaves before resolution")
    void animationDoesNothingAfterSourceLeaves() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, hiddenStag.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Hidden Stag");
        harness.assertNotOnBattlefield(player1, "Hidden Stag");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Hidden Stag");
        harness.assertNotOnBattlefield(player1, "Hidden Stag");
    }

    @Test
    @DisplayName("Copying an animated Hidden Stag copies its enchantment form, not its animation")
    void copyingAnimatedStagDoesNotCopyAnimation() {
        Permanent hiddenStag = harness.addToBattlefieldAndReturn(player1, new HiddenStag());
        prepareLandPlay(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenStag)).isTrue();

        prepareLandPlay(player1);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, hiddenStag.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(hiddenStag.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isEnchantment(gd, copy)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
    }

    private void prepareLandPlay(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
