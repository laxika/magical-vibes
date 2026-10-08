package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolarBlast.class, GlorySeeker.class})
class SolarBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlast()));
        addSpellMana(player1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to target creature")
    void deals3DamageToCreature() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new SolarBlast()));
        addSpellMana(player1);
        UUID targetId = harness.getPermanentId(player2, "Glory Seeker");

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling deals 1 damage to a chosen player and draws a card")
    void cyclingDealsDamageAndDraws() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Solar Blast");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling may deal no damage and still draws a card")
    void cyclingMayBeDeclined() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Solar Blast");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling may deal 1 damage to a target creature and draws a card")
    void cyclingDealsDamageToCreature() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        addCyclingMana(player1);
        UUID targetId = harness.getPermanentId(player2, "Glory Seeker");

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player1, "Solar Blast");
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling can damage its controller and chooses whether to deal damage before drawing")
    void cyclingCanDamageControllerBeforeDrawing() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Solar Blast");
        harness.assertNotInHand(player1, "Glory Seeker");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotInHand(player1, "Glory Seeker");
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Losing the cycling trigger's target does not stop the cycling draw")
    void cyclingDrawsWhenDamageTargetBecomesIllegal() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setHand(player2, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        addCyclingMana(player1);
        addSpellMana(player2);
        UUID targetId = harness.getPermanentId(player2, "Glory Seeker");

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
        harness.assertInGraveyard(player1, "Solar Blast");
        harness.assertInHand(player1, "Glory Seeker");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cycling marks exactly 1 damage on a creature")
    void cyclingMarksOneDamage() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        addCyclingMana(player1);
        UUID targetId = harness.getPermanentId(player2, "Glory Seeker");

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Glory Seeker").getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player1, "Glory Seeker");
    }
    private void addSpellMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void addCyclingMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
