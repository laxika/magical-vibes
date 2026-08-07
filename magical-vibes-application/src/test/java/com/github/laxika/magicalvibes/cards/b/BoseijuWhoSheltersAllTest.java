package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoseijuWhoSheltersAllTest extends BaseCardTest {

    private void addBoseiju(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new BoseijuWhoSheltersAll());
        }
    }

    @Test
    @DisplayName("Boseiju enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new BoseijuWhoSheltersAll()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Boseiju, Who Shelters All").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating taps Boseiju, pays 2 life and adds {C}")
    void activatingAddsColorlessAndPaysLife() {
        addBoseiju(1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        Permanent boseiju = findPermanent(player1, "Boseiju, Who Shelters All");
        assertThat(boseiju.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.getUncounterableGrantingManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A sorcery paid for with Boseiju's mana can't be countered")
    void sorceryPaidWithBoseijuManaCannotBeCountered() {
        addBoseiju(1);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.spellsMadeUncounterable).contains(divination.getId());

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, divination.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same sorcery paid for with ordinary mana is countered")
    void sorceryPaidWithOrdinaryManaIsCountered() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.spellsMadeUncounterable).doesNotContain(divination.getId());

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, divination.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boseiju's mana spent on a creature spell leaves that spell counterable")
    void creatureSpellPaidWithBoseijuManaIsStillCounterable() {
        addBoseiju(1);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);

        assertThat(gd.spellsMadeUncounterable).doesNotContain(bears.getId());
    }
}
