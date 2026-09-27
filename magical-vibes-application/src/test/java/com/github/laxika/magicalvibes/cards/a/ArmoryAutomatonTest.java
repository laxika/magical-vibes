package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoryAutomaton.class, AccordersShield.class})
class ArmoryAutomatonTest extends BaseCardTest {

    @Test
    void entersAndAttachesAnyTargetedEquipmentWithoutChangingControl() {
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        harness.setHand(player1, List.of(new ArmoryAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.handlePermanentChosen(player1, opposingEquipment.getId());
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Armory Automaton");
        assertThat(ownEquipment.getAttachedTo()).isEqualTo(automaton.getId());
        assertThat(opposingEquipment.getAttachedTo()).isEqualTo(automaton.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingEquipment);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownEquipment);
    }

    @Test
    void attackAttachesAnyTargetedEquipment() {
        Permanent automaton = addReady(player1, new ArmoryAutomaton());
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(automaton)));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.handlePermanentChosen(player1, opposingEquipment.getId());
        harness.passBothPriorities();

        assertThat(ownEquipment.getAttachedTo()).isEqualTo(automaton.getId());
        assertThat(opposingEquipment.getAttachedTo()).isEqualTo(automaton.getId());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
