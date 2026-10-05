package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GalvanicBombardment;
import com.github.laxika.magicalvibes.cards.t.ThermoAlchemist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakeMischief.class, GalvanicBombardment.class, ThermoAlchemist.class})
class MakeMischiefTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a player and creates a Devil token")
    void dealsDamageAndCreatesDevil() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MakeMischief()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Devil");
    }

    @Test
    @DisplayName("Devil token deals 1 damage to a chosen target when it dies")
    void devilDealsDamageWhenItDies() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MakeMischief()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent devil = findPermanent(player1, "Devil");
        harness.setHand(player1, List.of(new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, devil.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Devil");
    }

    @Test
    @DisplayName("Can damage its controller and still creates exactly one Devil")
    void canTargetController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MakeMischief()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(countPermanents(player1, "Devil")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Devil");
    }

    @Test
    @DisplayName("Spell and Devil death trigger can each damage a creature")
    void spellAndDeathTriggerCanTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThermoAlchemist());
        harness.setHand(player1, List.of(new MakeMischief(), new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(countPermanents(player1, "Devil")).isEqualTo(1);
        Permanent devil = findPermanent(player1, "Devil");
        harness.castInstant(player1, 0, devil.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Thermo-Alchemist");
        harness.assertNotOnBattlefield(player1, "Devil");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not create a Devil when its only target dies before resolution")
    void illegalTargetPreventsTokenCreation() {
        harness.setHand(player1, List.of(new MakeMischief()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        Permanent devil = findPermanent(player1, "Devil");

        harness.setHand(player1, List.of(new MakeMischief(), new GalvanicBombardment()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, devil.getId());
        harness.castInstant(player1, 0, devil.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devil");
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof MakeMischief).count()).isEqualTo(2);
    }
}
