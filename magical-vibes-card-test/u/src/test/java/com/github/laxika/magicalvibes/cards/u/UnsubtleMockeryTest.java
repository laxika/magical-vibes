package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnsubtleMockery.class, AirElemental.class, AvatarOfMight.class, GrizzlyBears.class})
class UnsubtleMockeryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature, killing a 4/4")
    void kills4ToughnessCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2); // 2 generic + 1 red

        harness.castAndResolveInstant(player1, 0, targetId);
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

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, false); // decline surveil

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(4);
    }

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

        harness.castAndResolveInstant(player1, 0, targetId);
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

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, false); // surveil: leave on top

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Unsubtle Mockery");
    }

    @Test
    @DisplayName("An empty library does not prevent the creature damage or require a surveil choice")
    void resolvesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Unsubtle Mockery");
    }

    @Test
    @DisplayName("Can target a creature you control and surveils only your library")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        Card ownTop = new GrizzlyBears();
        Card opponentTop = new AirElemental();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentTop);
    }

    @Test
    @DisplayName("Does not surveil when its only target becomes illegal")
    void doesNotSurveilWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UnsubtleMockery()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Unsubtle Mockery");
    }
}
