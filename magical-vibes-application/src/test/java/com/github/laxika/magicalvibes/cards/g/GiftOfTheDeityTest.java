package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiftOfTheDeityTest extends BaseCardTest {

    // ===== Black enchanted creature: +1/+1 and deathtouch =====

    @Test
    @DisplayName("Black creature gets +1/+1 and deathtouch")
    void blackCreatureGetsBoostAndDeathtouch() {
        Permanent black = attach(new WalkingCorpse());

        // Walking Corpse is 2/2; +1/+1 -> 3/3
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, black, Keyword.DEATHTOUCH)).isTrue();
    }

    // ===== Green enchanted creature: +1/+1 and lure =====

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
        Permanent green = new Permanent(new GrizzlyBears());
        green.setSummoningSick(false);
        green.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(green);
        Permanent gift = new Permanent(new GiftOfTheDeity());
        gift.setAttachedTo(green.getId());
        gd.playerBattlefields.get(player1.getId()).add(gift);

        Permanent blocker1 = readyCreature(new GrizzlyBears());
        Permanent blocker2 = readyCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(blocker1);
        gd.playerBattlefields.get(player2.getId()).add(blocker2);

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

    // ===== Creature that is neither black nor green: nothing applies =====

    @Test
    @DisplayName("White creature gets no boost, no deathtouch, and is not a lure")
    void whiteCreatureGetsNothing() {
        Permanent white = new Permanent(new HonorGuard());
        white.setSummoningSick(false);
        white.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(white);
        Permanent gift = new Permanent(new GiftOfTheDeity());
        gift.setAttachedTo(white.getId());
        gd.playerBattlefields.get(player1.getId()).add(gift);

        // Honor Guard is 1/1 and stays 1/1
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, white, Keyword.DEATHTOUCH)).isFalse();

        // Not a lure: a lone blocker is free to not block.
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiftOfTheDeity()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attach(com.github.laxika.magicalvibes.model.Card creature) {
        Permanent creaturePerm = new Permanent(creature);
        creaturePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creaturePerm);
        Permanent gift = new Permanent(new GiftOfTheDeity());
        gift.setAttachedTo(creaturePerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(gift);
        return creaturePerm;
    }

    private Permanent readyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
