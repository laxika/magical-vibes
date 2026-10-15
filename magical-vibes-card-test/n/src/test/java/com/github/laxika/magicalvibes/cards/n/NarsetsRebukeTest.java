package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarsetsRebuke.class, AvatarOfMight.class, GrizzlyBears.class, Shock.class})
class NarsetsRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage and exiles a creature instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new NarsetsRebuke()));
        addManaForSpell();

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Adds blue, red, and white mana after dealing damage")
    void addsMana() {
        harness.addToBattlefield(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new NarsetsRebuke()));
        addManaForSpell();

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Marks a surviving creature for exile if it dies later this turn")
    void marksSurvivingCreature() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new NarsetsRebuke()));
        addManaForSpell();

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(avatar.getMarkedDamage()).isEqualTo(5);
        assertThat(avatar.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Exiles a surviving creature when later damage kills it this turn")
    void exilesCreatureKilledLaterThisTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new NarsetsRebuke(), new Shock(), new Shock()));
        addManaForSpell();

        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(harness.getGameData().exiledCards)
                .anyMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("Does not add mana when its only target becomes illegal")
    void doesNotAddManaWhenTargetDiesInResponse() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NarsetsRebuke()));
        harness.setHand(player2, List.of(new Shock()));
        addManaForSpell();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Narset's Rebuke");
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Exile replacement and marked damage expire at the end of the turn")
    void replacementExpiresAtEndOfTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new NarsetsRebuke()));
        addManaForSpell();
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(harness.getGameData().exiledCards)
                .noneMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);
    }
}
