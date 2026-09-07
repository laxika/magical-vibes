package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfTheParuns.class, GrizzlyBears.class})
class SwordOfTheParunsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped equipped creature gives tapped creatures +2/+0")
    void tappedEquippedCreatureBoostsTappedCreatures() {
        Permanent sword = addSwordReady(player1);
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(tappedCreature.getId());
        tappedCreature.tap();

        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapped equipped creature gives untapped creatures +0/+2")
    void untappedEquippedCreatureBoostsUntappedCreatures() {
        Permanent sword = addSwordReady(player1);
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(untappedCreature.getId());
        tappedCreature.tap();

        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability lets you choose to tap the equipped creature")
    void activatedAbilityCanTapEquippedCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap equipped creature");

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability lets you choose to untap the equipped creature")
    void activatedAbilityCanUntapEquippedCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(creature.getId());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Untap equipped creature");

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the activated ability does not change the equipped creature's tap state")
    void decliningActivatedAbilityDoesNothing() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = new Permanent(new SwordOfTheParuns());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sword);
        return sword;
    }
}
