package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsatiableSouleater.class})
class InsatiableSouleaterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating trample ability puts it on the stack")
    void activatingTramplePutsOnStack() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(souleater.getId());
        assertThat(entry.getTargetId()).isEqualTo(souleater.getId());
    }

    @Test
    @DisplayName("Resolving trample ability grants trample until end of turn")
    void resolvingTrampleAbilityGrantsTrample() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample granted by ability resets at end of turn cleanup")
    void trampleResetsAtEndOfTurn() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Can pay Phyrexian mana with 2 life when no green mana available")
    void paysLifeWhenNoGreenMana() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);
        // No mana added — Phyrexian mana auto-pays with 2 life

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prefers green mana over life payment when available")
    void prefersGreenManaOverLife() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isTrue();
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability does NOT tap Insatiable Souleater")
    void activatingAbilityDoesNotTap() {
        Permanent souleater = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(souleater.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent souleater = addSouleaterReady(player1);
        souleater.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(souleater.getId());
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        Permanent souleater = harness.addToBattlefieldAndReturn(player1, new InsatiableSouleater());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(souleater.getId());
    }

    @Test
    @DisplayName("Ability has no effect if Insatiable Souleater is removed before resolution")
    void abilityHasNoEffectIfSourceRemoved() {
        addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the Phyrexian cost with less than two life and no green mana")
    void cannotPayWithInsufficientLife() {
        Permanent souleater = addSouleaterReady(player1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, souleater, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only the activated Souleater gains trample")
    void grantsTrampleOnlyToSource() {
        Permanent source = addSouleaterReady(player1);
        Permanent other = addSouleaterReady(player1);
        Permanent opponent = addSouleaterReady(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An old activation does not grant trample to the source after it reenters")
    void oldActivationDoesNotAffectReturnedSource() {
        Permanent source = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, source.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addSouleaterReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new InsatiableSouleater());
        perm.setSummoningSick(false);
        return perm;
    }
}
