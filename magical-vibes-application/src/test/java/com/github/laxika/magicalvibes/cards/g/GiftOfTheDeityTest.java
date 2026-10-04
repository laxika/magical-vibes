package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.w.WoodlurkerMimic;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfTheDeity.class, WalkingCorpse.class, GrizzlyBears.class,
        HonorGuard.class, FountainOfYouth.class, WoodlurkerMimic.class})
class GiftOfTheDeityTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature gets +1/+1 and deathtouch")
    void blackCreatureGetsBoostAndDeathtouch() {
        Permanent black = attach(new WalkingCorpse());

        // Walking Corpse is 2/2; +1/+1 -> 3/3
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, black, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Green creature gets +1/+1 but not deathtouch")
    void greenCreatureGetsBoostNotDeathtouch() {
        Permanent green = attach(new GrizzlyBears());

        // Grizzly Bears is 2/2; +1/+1 -> 3/3
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, green, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("All able creatures must block a green enchanted attacker")
    void greenCreatureMustBeBlockedByAll() {
        Permanent green = attach(new GrizzlyBears());
        green.setAttacking(true);

        Permanent blocker1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blocker2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("White creature gets no boost, no deathtouch, and is not a lure")
    void whiteCreatureGetsNothing() {
        Permanent white = attach(new HonorGuard());
        white.setAttacking(true);

        // Honor Guard is 1/1 and stays 1/1
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, white, Keyword.DEATHTOUCH)).isFalse();

        // Not a lure: a lone blocker is free to not block.
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiftOfTheDeity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }


    @Test
    @DisplayName("Both color bonuses apply after the Aura resolves on a black and green creature")
    void bothColorBonusesApplyAfterResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlurkerMimic());
        harness.setHand(player1, List.of(new GiftOfTheDeity()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(findPermanent(player1, "Gift of the Deity").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Tapped creatures are not required to block a green enchanted creature")
    void tappedCreatureIsNotRequiredToBlock() {
        Permanent attacker = attach(new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent able = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tapped.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(able.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    private Permanent attach(com.github.laxika.magicalvibes.model.Card creature) {
        Permanent creaturePerm = harness.addToBattlefieldAndReturn(player1, creature);
        creaturePerm.setSummoningSick(false);
        Permanent gift = harness.addToBattlefieldAndReturn(player1, new GiftOfTheDeity());
        gift.setAttachedTo(creaturePerm.getId());
        return creaturePerm;
    }

}
