package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SureStrike;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraviticPunch.class, HillGiant.class, GrizzlyBears.class, Plains.class,
        ChildOfNight.class, SureStrike.class, Unsummon.class})
class GraviticPunchTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control deals damage equal to its power to a target player")
    void creatureDamagesTargetPlayer() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new GraviticPunch()));
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(giantId, player2.getId()));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a creature as the player target")
    void cannotTargetCreatureAsPlayer() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraviticPunch()));
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(giantId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("Jump-start discards a card, deals damage, and exiles the spell")
    void jumpStartDiscardsDealsDamageAndExiles() {
        GraviticPunch spell = new GraviticPunch();
        Plains discarded = new Plains();
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        gs.playFlashbackSpell(gd, player1, 0, null, null,
                List.of(giantId, player2.getId()), null, null, List.of(), 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot use an opponent's creature as the damage source")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GraviticPunch()));
        addMana();

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(giantId, player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller may be the player target")
    void canDamageController() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new GraviticPunch()));
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(giantId, player1.getId()));

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Gravitic Punch");
    }

    @Test
    @DisplayName("Damage uses the creature's power at resolution")
    void usesPowerAtResolution() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new GraviticPunch(), new SureStrike()));
        addMana();
        harness.addMana(player1, ManaColor.RED, 2);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(giantId, player2.getId()));
        harness.castAndResolveInstant(player1, 0, giantId);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The creature is the damage source, so its lifelink gains life")
    void creatureLifelinkApplies() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.setHand(player1, List.of(new GraviticPunch()));
        addMana();

        UUID childId = harness.getPermanentId(player1, "Child of Night");
        harness.castAndResolveSorcery(player1, 0, List.of(childId, player2.getId()));

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A creature that leaves before resolution deals no damage")
    void removedCreatureDealsNoDamage() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new GraviticPunch()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(giantId, player2.getId()));
        harness.castAndResolveInstant(player2, 0, giantId);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Gravitic Punch");
    }

    @Test
    @DisplayName("Jump-start requires discarding a card")
    void jumpStartRequiresDiscard() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GraviticPunch()));
        harness.setHand(player1, List.of());
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null,
                List.of(giantId, player2.getId()), null, null, List.of(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");
        harness.assertInGraveyard(player1, "Gravitic Punch");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Jump-start may discard a nonland and still exiles when the creature leaves")
    void jumpStartExilesWithRemovedCreature() {
        GraviticPunch spell = new GraviticPunch();
        GraviticPunch discarded = new GraviticPunch();
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        gs.playFlashbackSpell(gd, player1, 0, null, null,
                List.of(giantId, player2.getId()), null, null, List.of(), 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId()).contains(discarded.getId()).doesNotContain(spell.getId());
        harness.castAndResolveInstant(player2, 0, giantId);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId()).contains(discarded.getId()).doesNotContain(spell.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
