package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResurrectionOrb.class, GrizzlyBears.class, Deathmark.class})
class ResurrectionOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has lifelink")
    void equippedCreatureHasLifelink() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        orb.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Equip {4} attaches Resurrection Orb to a creature you control")
    void equipsToCreatureYouControl() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(orb), null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(orb.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature returns under its owner's control at the next end step")
    void returnsEquippedCreatureAtNextEndStep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ResurrectionOrb());
        orb.setAttachedTo(creature.getId());

        destroyWithDeathmark(creature);

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(orb.getAttachedTo()).isNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
    }

    private void destroyWithDeathmark(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
