package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.h.HavengulLich;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.y.YawgmothsAgenda;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchfiendsVessel.class, Shock.class, YawgmothsAgenda.class, Zombify.class, RiseFromTheGrave.class,
        HavengulLich.class})
class ArchfiendsVesselTest extends BaseCardTest {

    @Test
    @DisplayName("A normal cast does not create a Demon")
    void normalCastDoesNotCreateDemon() {
        harness.setHand(player1, List.of(new ArchfiendsVessel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Archfiend's Vessel");
        assertThat(countPermanents(player1, "Demon")).isZero();
    }

    @Test
    @DisplayName("Casting it from a graveyard exiles it and creates a flying Demon")
    void castFromGraveyardExilesItAndCreatesDemon() {
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setGraveyard(player1, List.of(new ArchfiendsVessel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Archfiend's Vessel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Archfiend's Vessel"));
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an opponent's Vessel from their graveyard does not create a Demon")
    void castingFromOpponentGraveyardDoesNotCreateDemon() {
        harness.addToBattlefield(player1, new HavengulLich());
        var vessel = new ArchfiendsVessel();
        harness.setGraveyard(player2, List.of(vessel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, vessel.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromGraveyard(player1, vessel.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Archfiend's Vessel");
        harness.assertNotInGraveyard(player2, "Archfiend's Vessel");
        assertThat(countPermanents(player1, "Demon")).isZero();
    }

    @Test
    @DisplayName("Casting your own Vessel with Havengul Lich creates exactly one Demon")
    void castingFromOwnGraveyardWithLichCreatesDemon() {
        harness.addToBattlefield(player1, new HavengulLich());
        var vessel = new ArchfiendsVessel();
        harness.setGraveyard(player1, List.of(vessel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, vessel.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromGraveyard(player1, vessel.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Archfiend's Vessel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(vessel.getId()));
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning it from a graveyard exiles it and creates a flying Demon")
    void returningFromGraveyardExilesItAndCreatesDemon() {
        var vessel = new ArchfiendsVessel();
        harness.setGraveyard(player1, List.of(vessel));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, vessel.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Archfiend's Vessel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Archfiend's Vessel"));
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
    }

    @Test
    @DisplayName("If it leaves before its trigger resolves, it does not create a Demon")
    void leavingBeforeTriggerResolutionDoesNotCreateDemon() {
        var vessel = new ArchfiendsVessel();
        harness.setGraveyard(player1, List.of(vessel));
        harness.setHand(player1, List.of(new Zombify(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, vessel.getId());
        harness.passBothPriorities();

        var vesselPermanentId = harness.getPermanentId(player1, "Archfiend's Vessel");
        harness.castInstant(player1, 0, vesselPermanentId);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Archfiend's Vessel");
        assertThat(countPermanents(player1, "Demon")).isZero();
    }

    @Test
    @DisplayName("Returning an opponent's Vessel under your control does not trigger it")
    void returningFromOpponentGraveyardDoesNotCreateDemon() {
        harness.setGraveyard(player2, List.of(new ArchfiendsVessel()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Archfiend's Vessel");
        harness.assertNotInGraveyard(player2, "Archfiend's Vessel");
        assertThat(countPermanents(player1, "Demon")).isZero();
    }

    @Test
    @DisplayName("Reanimation creates a 5/5 black flying Demon without lifelink")
    void createdDemonHasCorrectCharacteristics() {
        var vessel = new ArchfiendsVessel();
        harness.setGraveyard(player1, List.of(vessel));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, vessel.getId());
        resolveAllTriggers();

        var demon = findPermanent(player1, "Demon");
        assertThat(demon).isNotNull();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(5);
        assertThat(demon.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON);
        assertThat(gqs.hasKeyword(gd, demon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, demon, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The Vessel gains life when it deals combat damage")
    void combatDamageGainsLife() {
        addCreatureReady(player1, new ArchfiendsVessel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
