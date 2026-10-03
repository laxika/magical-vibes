package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
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

@CardUsed({DeviantGlee.class, DrudgeBeetle.class, PithingNeedle.class})
class DeviantGleeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Deviant Glee attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());

        harness.setHand(player1, List.of(new DeviantGlee()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Deviant Glee")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and loses it when the aura leaves")
    void boostAppliesAndStops() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeviantGlee());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature can pay {R} to gain trample until end of turn")
    void grantedAbilityGivesTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeviantGlee());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeviantGlee());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Deviant Glee cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.setHand(player1, List.of(new DeviantGlee()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Pithing Needle");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opponent's enchanted creature can activate the granted ability")
    void opponentsCreatureControlsGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new DeviantGlee()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        creature.setTapped(true);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An activated ability resolves after the granting aura leaves")
    void grantedAbilitySurvivesAuraRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeviantGlee());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The granted ability requires red mana")
    void grantedAbilityRequiresRedMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeviantGlee());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Deviant Glee goes to the graveyard if its target leaves before resolution")
    void auraDoesNotResolveWithoutItsTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new DeviantGlee()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof DeviantGlee);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof DeviantGlee);
    }
}
