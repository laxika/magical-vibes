package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wrench.class, SanitationAutomaton.class, Forest.class})
class WrenchTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1, vigilance, and the granted tap ability")
    void equippedCreatureGetsAbilities() {
        Permanent creature = addCreatureReady(player1, new SanitationAutomaton());
        Permanent wrench = addCreatureReady(player1, new Wrench());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        wrench.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        Permanent target = addCreatureReady(player2, new SanitationAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(player1, creature), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip {2} attaches Wrench to a creature you control")
    void equipsToControlledCreature() {
        Permanent wrench = addCreatureReady(player1, new Wrench());
        Permanent creature = addCreatureReady(player1, new SanitationAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, wrench), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wrench.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Sacrificing Wrench draws a card")
    void sacrificesAndDraws() {
        Permanent wrench = addCreatureReady(player1, new Wrench());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, wrench), 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Wrench");
    }

    @Test
    @DisplayName("A summoning-sick creature cannot use the granted tap ability")
    void grantedAbilityRequiresCreatureToBeReady() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        creature.setSummoningSick(true);
        Permanent wrench = addCreatureReady(player1, new Wrench());
        wrench.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new SanitationAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, creature), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted ability can tap a creature you control")
    void grantedAbilityCanTargetFriendlyCreature() {
        Permanent creature = addCreatureReady(player1, new SanitationAutomaton());
        Permanent wrench = addCreatureReady(player1, new Wrench());
        wrench.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player1, new SanitationAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, creature), 0, null, target.getId());
        assertThat(creature.isTapped()).isTrue();
        assertThat(wrench.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Wrench removes its bonuses but does not stop an activated tap ability")
    void activatedAbilitySurvivesWrenchSacrifice() {
        Permanent creature = addCreatureReady(player1, new SanitationAutomaton());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        Permanent wrench = addCreatureReady(player1, new Wrench());
        wrench.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new SanitationAutomaton());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, battlefieldIndex(player1, creature), 0, null, target.getId());
        harness.activateAbility(player1, battlefieldIndex(player1, wrench), 0, null, null);

        harness.assertInGraveyard(player1, "Wrench");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        resolveAllTriggers();
        harness.assertInHand(player1, "Forest");
        assertThat(target.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1,
                battlefieldIndex(player1, creature), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
