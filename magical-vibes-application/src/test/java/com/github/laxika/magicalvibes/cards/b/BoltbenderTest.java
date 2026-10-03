package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.p.PsionicBlast;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Boltbender.class, PsionicBlast.class, ProdigalSorcerer.class, SeedsOfStrength.class})
class BoltbenderTest extends BaseCardTest {

    @Test
    void turningFaceUpCanRetargetMultipleSpellsAndAbilities() {
        Permanent boltbender = castFaceDownBoltbender();
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        PsionicBlast psionicBlast = new PsionicBlast();
        harness.setHand(player1, List.of(psionicBlast));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null, player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(boltbender));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void disguiseTriggersWardWhenAnOpponentTargetsIt() {
        Permanent boltbender = castFaceDownBoltbender();
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer), null, boltbender.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void mayLeaveASpellsTargetUnchanged() {
        Permanent boltbender = castFaceDownBoltbender();
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(boltbender));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
    }

    @Test
    void mayChooseACreatureAlreadyUsedByAnotherTargetSlot() {
        Permanent boltbender = castFaceDownBoltbender();
        Permanent first = addCreatureReady(player1, new ProdigalSorcerer());
        Permanent second = addCreatureReady(player1, new ProdigalSorcerer());
        addCreatureReady(player1, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, List.of(boltbender.getId(), first.getId(), second.getId()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(boltbender));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(first.getId());
    }

    private Permanent castFaceDownBoltbender() {
        harness.setHand(player1, List.of(new Boltbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Boltbender");
    }
}
