package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DragonThroneOfTarkirTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has defender and the granted tap ability")
    void equippedCreatureGetsStaticAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);
        throne.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability boosts other creatures you control and gives them trample")
    void grantedAbilityBoostsOtherOwnCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);
        throne.setAttachedTo(creature.getId());
        creature.setPowerModifier(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The granted boost wears off at end of turn")
    void grantedBoostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent throne = addThroneReady(player1);
        throne.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addThroneReady(Player player) {
        Permanent perm = new Permanent(new DragonThroneOfTarkir());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
