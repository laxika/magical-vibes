package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillianDecisiveMentor.class, GloriousAnthem.class, HolyStrength.class, GrizzlyBears.class})
class KillianDecisiveMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and goads a target creature when an enchantment enters")
    void tapsAndGoadsTargetCreature() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws once when one or more enchanted creatures attack")
    void drawsOnceForMultipleEnchantedAttackers() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        firstAura.setAttachedTo(first.getId());
        secondAura.setAttachedTo(second.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when an opponent attacks a creature enchanted by an Aura you control")
    void drawsForOpponentCreatureEnchantedByControlledAura() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        aura.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void goadsAnAlreadyTappedCreatureWhenAnAuraEnters() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setTapped(true);
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantmentEntering() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotDrawForAnUnenchantedAttacker() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawForAnAttackerEnchantedOnlyByAnOpponentsAura() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(attacker.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void mayChooseNoCreatureWhenAnEnchantmentEnters() {
        harness.addToBattlefield(player1, new KillianDecisiveMentor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
