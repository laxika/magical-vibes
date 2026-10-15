package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpreadingRot.class, Mountain.class, JungleDelver.class, DarksteelCitadel.class})
class SpreadingRotTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Spreading Rot puts it on the stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Spreading Rot");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target land and its controller loses 2 life")
    void destroysLandAndControllerLosesLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Caster's life total is unchanged")
    void casterLifeUnchanged() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Spreading Rot goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Spreading Rot");
    }

    @Test
    @DisplayName("Fizzles when target land is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature with Spreading Rot")
    void cannotTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new JungleDelver()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy your own land and makes you lose life")
    void destroysOwnLandAndLosesLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Indestructible land survives but its controller still loses life")
    void indestructibleLandControllerStillLosesLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel()).getId();
        harness.setHand(player1, List.of(new SpreadingRot()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Spreading Rot");
    }
}
