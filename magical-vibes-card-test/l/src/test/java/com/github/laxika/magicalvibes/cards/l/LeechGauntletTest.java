package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeechGauntlet.class, JukaiTrainee.class, EnchantedEvening.class})
class LeechGauntletTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsLifelink() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        gauntlet.setAttachedTo(creature.getId());

        assertThat(gqs.isCreature(gd, gauntlet)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheGauntlet() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, gauntlet)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, gauntlet)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent opponentCreature = addCreatureReady(player2, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gauntlet.getAttachedTo()).isNull();
    }

    @Test
    void unattachedGauntletGainsLifeFromCombatDamage() {
        addCreatureReady(player1, new LeechGauntlet());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void equippedCreatureGainsLifeFromCombatDamage() {
        addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void reconfigureMovesLifelinkToAnotherCreature() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent first = addCreatureReady(player1, new JukaiTrainee());
        Permanent second = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isCreature(gd, gauntlet)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void reconfigureCannotTargetItself() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, gauntlet.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gauntlet.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unattachCannotBeActivatedWhileUnattached() {
        addCreatureReady(player1, new LeechGauntlet());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothReconfigureAbilitiesRequireSorceryTiming() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        gauntlet.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reconfigureRequiresFourMana() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gauntlet.getAttachedTo()).isNull();
    }

    @Test
    @CardUsed({EnchantedEvening.class})
    void attachedGauntletRetainsItsOtherCardTypes() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, gauntlet)).isFalse();
        assertThat(gqs.isArtifact(gd, gauntlet)).isTrue();
        assertThat(gqs.isEnchantment(gd, gauntlet)).isTrue();
    }
}
