package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudExSOLDIER.class, GrizzlyBears.class, LeoninScimitar.class})
class CloudExSOLDIERTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches up to one target Equipment you control when Cloud enters")
    void attachesTargetEquipmentOnEntry() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent cloud = harness.enterBattlefieldAndReturn(player1, new CloudExSOLDIER());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(equipment.getId());

        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(cloud.getId());
    }

    @Test
    @DisplayName("Draws for each equipped attacking creature you control")
    void drawsForEachEquippedAttackingCreature() {
        Permanent cloud = addCreatureReady(new CloudExSOLDIER());
        Permanent bear = addCreatureReady(new GrizzlyBears());
        Permanent nonattackingBear = addCreatureReady(new GrizzlyBears());
        attachEquipment(cloud);
        attachEquipment(bear);
        attachEquipment(nonattackingBear);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card(), new Card(), new Card()));

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(cloud),
                gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates two Treasures when Cloud attacks with power 7 or greater")
    void createsTreasuresAtHighPower() {
        Permanent cloud = addCreatureReady(new CloudExSOLDIER());
        attachEquipment(cloud);
        cloud.setPowerModifier(3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cloud)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    private Permanent addCreatureReady(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
    }
}
