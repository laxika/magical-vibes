package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornedHelm.class, Arachnoid.class})
class HornedHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Unattached Horned Helm does not affect creatures")
    void unattachedHelmDoesNotAffectCreatures() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        addHelmReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Green ability attaches Horned Helm at instant speed")
    void greenAbilityAttachesAtInstantSpeed() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Green ability cannot target a creature controlled by an opponent")
    void greenAbilityRequiresCreatureYouControl() {
        addHelmReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip ability cannot target a creature controlled by an opponent")
    void equipAbilityRequiresCreatureYouControl() {
        addHelmReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip ability can only be activated at sorcery speed")
    void equipAbilityRequiresSorcerySpeed() {
        addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip ability fizzles if its target leaves before resolution")
    void equipAbilityFizzlesIfTargetLeavesBeforeResolution() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip ability attaches Horned Helm for one mana")
    void equipAbilityAttachesForOneMana() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addHelmReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HornedHelm());
    }
}
