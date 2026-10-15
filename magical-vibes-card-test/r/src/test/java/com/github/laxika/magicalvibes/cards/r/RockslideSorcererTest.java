package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RockslideSorcerer.class, Shock.class, Divination.class, FugitiveWizard.class, GrizzlyBears.class})
class RockslideSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 additional damage when its controller casts an instant")
    void triggersForInstant() {
        addRockslideSorcerer();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 1 damage when its controller casts a sorcery")
    void triggersForSorcery() {
        addRockslideSorcerer();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage when its controller casts a Wizard spell")
    void triggersForWizard() {
        addRockslideSorcerer();
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger for a non-Wizard creature spell")
    void doesNotTriggerForNonWizardCreature() {
        addRockslideSorcerer();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @CardUsed({RockslideSorcerer.class})
    @DisplayName("Does not trigger for its own cast before entering the battlefield")
    void doesNotTriggerForItsOwnCast() {
        harness.setHand(player1, List.of(new RockslideSorcerer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Rockslide Sorcerer");
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({RockslideSorcerer.class})
    @DisplayName("An existing Sorcerer triggers for another Sorcerer and can target its controller")
    void triggersForAnotherSorcererAndCanTargetController() {
        addRockslideSorcerer();
        harness.setHand(player1, List.of(new RockslideSorcerer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(player1.getId());

        harness.assertLife(player1, 19);
        assertThat(countPermanents(player1, "Rockslide Sorcerer")).isEqualTo(2);
    }

    @Test
    @CardUsed({RockslideSorcerer.class})
    @DisplayName("Can target itself with its damage trigger")
    void canTargetItself() {
        addRockslideSorcerer();
        var source = findPermanent(player1, "Rockslide Sorcerer");
        harness.setHand(player1, List.of(new RockslideSorcerer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(source.getId());

        assertThat(source.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({RockslideSorcerer.class})
    @DisplayName("Does not trigger for an opponent's Wizard spell")
    void doesNotTriggerForOpponentsWizard() {
        addRockslideSorcerer();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RockslideSorcerer()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Rockslide Sorcerer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
    private void addRockslideSorcerer() {
        harness.addToBattlefield(player1, new RockslideSorcerer());
    }

    private void chooseTriggerTarget(java.util.UUID targetId) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, targetId);
        resolveAllTriggers();
    }
}
