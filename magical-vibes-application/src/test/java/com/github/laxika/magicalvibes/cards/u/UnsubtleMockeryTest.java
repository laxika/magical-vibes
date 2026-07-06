package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnsubtleMockeryTest extends BaseCardTest {

    // ===== Damage =====

    @Test
    @DisplayName("Deals 4 damage to target creature, killing a 4/4")
    void kills4ToughnessCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2); // 2 generic + 1 red

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline surveil

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Deals exactly 4 marked damage to a surviving creature")
    void marks4DamageOnSurvivor() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline surveil

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(4);
    }

    // ===== Surveil =====

    @Test
    @DisplayName("Surveil puts top card into graveyard when accepted")
    void surveilAcceptedMillsTopCard() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // surveil: put top card into graveyard

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Surveil leaves top card on the library when declined")
    void surveilDeclinedLeavesTopCard() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // surveil: leave on top

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Unsubtle Mockery");
    }
}
