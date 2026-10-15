package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.s.SlaughterDrone;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed({UntamedHunger.class, SlaughterDrone.class, BoneSaw.class})
class UntamedHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Untamed Hunger attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        harness.setHand(player1, List.of(new UntamedHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof UntamedHunger
                        && target.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and menace")
    void enchantedCreatureGetsBoostAndMenace() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        aura.setAttachedTo(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Effects stop when Untamed Hunger is removed")
    void effectsStopWhenRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        aura.setAttachedTo(target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Untamed Hunger does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        aura.setAttachedTo(target.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Untamed Hunger fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        harness.setHand(player1, List.of(new UntamedHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Untamed Hunger");
        harness.assertNotOnBattlefield(player1, "Untamed Hunger");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Untamed Hunger")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        harness.setHand(player1, List.of(new UntamedHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canEnchantOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        harness.setHand(player1, List.of(new UntamedHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Untamed Hunger").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void multipleAurasStackBonusesAndRemainingAuraKeepsMenace() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        first.setAttachedTo(target.getId());
        second.setAttachedTo(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void auraGoesToGraveyardWhenEnchantedCreatureLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        aura.setAttachedTo(target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Untamed Hunger");
        harness.assertNotOnBattlefield(player1, "Untamed Hunger");
    }

    @Test
    void enchantedCreatureCannotBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new SlaughterDrone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        aura.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new SlaughterDrone());
        addCreatureReady(player2, new SlaughterDrone());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void enchantedCreatureCanBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new SlaughterDrone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UntamedHunger());
        aura.setAttachedTo(attacker.getId());
        Permanent first = addCreatureReady(player2, new SlaughterDrone());
        Permanent second = addCreatureReady(player2, new SlaughterDrone());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
