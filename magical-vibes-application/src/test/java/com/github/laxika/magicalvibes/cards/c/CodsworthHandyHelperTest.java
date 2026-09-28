package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CodsworthHandyHelper.class, BoneSaw.class, GrizzlyBears.class, IntangibleVirtue.class,
        Pacifism.class, Shock.class})
class CodsworthHandyHelperTest extends BaseCardTest {

    @Test
    void grantsWardToCommandersYouControl() {
        addCodsworthReady(player1);
        Permanent commander = addCreatureReady(player1, new GrizzlyBears());
        gd.makeCommander(player1.getId(), commander.getCard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, commander.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void restrictedManaCastsAurasAndEquipmentOnly() {
        Permanent codsworth = addCodsworthReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        int codsworthIndex = battlefieldIndex(codsworth);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, codsworthIndex, 0, null, null);
        harness.setHand(player1, List.of(new Pacifism()));
        harness.castEnchantment(player1, 0, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pacifism").isAttached()).isTrue();

        codsworth.untap();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, codsworthIndex, 0, null, null);
        harness.setHand(player1, List.of(new BoneSaw()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bone Saw")).isNotNull();

        codsworth.untap();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, codsworthIndex, 0, null, null);
        harness.setHand(player1, List.of(new IntangibleVirtue()));
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attachesAnUnattachedEquipmentAtSorcerySpeed() {
        Permanent codsworth = addCodsworthReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BoneSaw());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(codsworth), 1,
                List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());

        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(opposingCreature.getId());
        codsworth.untap();
        harness.clearPriorityPassed();
        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(codsworth), 1,
                List.of(aura.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addCodsworthReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new CodsworthHandyHelper());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
