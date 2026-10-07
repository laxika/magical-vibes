package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteelOfTheGodhead.class, EliteVanguard.class, FountainOfYouth.class,
        FugitiveWizard.class, GrizzlyBears.class, Somnomancer.class})
class SteelOfTheGodheadTest extends BaseCardTest {

    @Test
    void whiteAndBlueCreatureGetsBothBonuses() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Somnomancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SteelOfTheGodhead());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    @Test
    void lifelinkGainsLifeForCreatureControllerRatherThanAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Somnomancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SteelOfTheGodhead());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        gd.currentStep = TurnStep.COMBAT_DAMAGE;
        creature.setAttacking(true);
        creature.setAttackTarget(player1.getId());

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Resolving Steel of the Godhead attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        harness.setHand(player1, List.of(new SteelOfTheGodhead()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Steel of the Godhead")
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    @Test
    @DisplayName("White enchanted creature gets +1/+1 and lifelink, but is not unblockable")
    void whiteCreatureGetsBoostAndLifelink() {
        Permanent white = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        Permanent steel = harness.addToBattlefieldAndReturn(player1, new SteelOfTheGodhead());
        steel.setAttachedTo(white.getId());

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, white)).isFalse();
    }

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1 and can't be blocked, but no lifelink")
    void blueCreatureGetsBoostAndUnblockable() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        Permanent steel = harness.addToBattlefieldAndReturn(player1, new SteelOfTheGodhead());
        steel.setAttachedTo(blue.getId());

        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, blue)).isTrue();
        assertThat(gqs.hasKeyword(gd, blue, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("A creature that is neither white nor blue gets no bonuses")
    void nonWhiteNonBlueGetsNothing() {
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent steel = harness.addToBattlefieldAndReturn(player1, new SteelOfTheGodhead());
        steel.setAttachedTo(green.getId());

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, green, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, green)).isFalse();
    }

    @Test
    @DisplayName("Bonuses are removed when Steel of the Godhead leaves the battlefield")
    void bonusesRemovedWhenAuraRemoved() {
        Permanent white = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        Permanent steel = harness.addToBattlefieldAndReturn(player1, new SteelOfTheGodhead());
        steel.setAttachedTo(white.getId());

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(steel);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Steel of the Godhead")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SteelOfTheGodhead()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
