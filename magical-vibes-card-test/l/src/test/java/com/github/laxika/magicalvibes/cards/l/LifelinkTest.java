package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lifelink.class, RuneclawBear.class, Mountain.class})
class LifelinkTest extends BaseCardTest {


    @Test
    @DisplayName("Casting Lifelink puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Lifelink card = new Lifelink();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Resolving Lifelink attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lifelink")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has lifelink")
    void enchantedCreatureHasLifelink() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent lifelinkPerm = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature gains controller life equal to combat damage dealt")
    void gainsLifeOnCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent lifelinkPerm = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Player1 gains 2 life (bears power), player2 loses 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creature loses lifelink when Lifelink aura is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent lifelinkPerm = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(lifelinkPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent otherBears = addCreatureReady(player1, new RuneclawBear());

        Permanent lifelinkPerm = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        lifelinkPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lifelink");
        harness.assertNotOnBattlefield(player1, "Lifelink");
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    @Test
    @DisplayName("Enchanting an opposing creature gives life to its controller")
    void opposingCreatureControllerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Two Lifelink Auras do not double the life gained")
    void multipleLifelinkAurasAreRedundant() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        firstAura.setAttachedTo(bear.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        secondAura.setAttachedTo(bear.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
    @Test
    @CardUsed({ProdigalPyromancer.class})
    @DisplayName("Enchanted creature gains life from noncombat damage")
    void gainsLifeFromNoncombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new Lifelink()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, pyromancer.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
