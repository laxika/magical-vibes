package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EtherealArmor;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaazdaExonerator.class, EtherealArmor.class, GloriousAnthem.class, GrizzlyBears.class})
class HaazdaExoneratorTest extends BaseCardTest {

    @Test
    void sacrificesItselfToDestroyTargetAura() {
        Permanent exonerator = addCreatureReady(player1, new HaazdaExonerator());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        aura.setAttachedTo(creature.getId());
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(exonerator, aura);
        harness.assertInGraveyard(player1, "Haazda Exonerator");
        harness.assertInGraveyard(player1, "Ethereal Armor");
    }

    @Test
    void cannotTargetNonAuraEnchantment() {
        addCreatureReady(player1, new HaazdaExonerator());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, anthem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
