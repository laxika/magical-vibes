package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssertAuthority.class, AuriokTransfixer.class, Bonesplitter.class, ElectrostaticBolt.class})
class AssertAuthorityTest extends BaseCardTest {

    @Test
    void countersCreatureSpellAndExilesIt() {
        AuriokTransfixer transfixer = new AuriokTransfixer();
        harness.setHand(player1, List.of(transfixer));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new AssertAuthority()));
        harness.addMana(player2, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, transfixer.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Auriok Transfixer"));
        harness.assertNotInGraveyard(player1, "Auriok Transfixer");
        harness.assertNotOnBattlefield(player1, "Auriok Transfixer");
    }

    @Test
    void countersNonCreatureSpellAndExilesIt() {
        AuriokTransfixer transfixer = new AuriokTransfixer();
        harness.addToBattlefield(player1, transfixer);

        ElectrostaticBolt bolt = new ElectrostaticBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new AssertAuthority()));
        harness.addMana(player2, ManaColor.BLUE, 7);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Auriok Transfixer"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Electrostatic Bolt"));
        harness.assertNotInGraveyard(player1, "Electrostatic Bolt");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Electrostatic Bolt"));
    }

    @Test
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Bonesplitter());
        }

        AuriokTransfixer transfixer = new AuriokTransfixer();
        harness.setHand(player1, List.of(transfixer));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.setHand(player2, List.of(new AssertAuthority()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, transfixer.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void affinityCountsOnlyArtifactsControlledBySpellCaster() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Bonesplitter());
        }

        AuriokTransfixer transfixer = new AuriokTransfixer();
        harness.setHand(player1, List.of(transfixer));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new AssertAuthority()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, transfixer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void affinityDoesNotCountNonArtifacts() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Bonesplitter());
        }
        harness.addToBattlefield(player2, new AuriokTransfixer());

        AuriokTransfixer transfixer = new AuriokTransfixer();
        harness.setHand(player1, List.of(transfixer));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new AssertAuthority()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, transfixer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
