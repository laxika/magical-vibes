package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
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

@CardUsed({MawOfTheMire.class, Mountain.class, AmbushViper.class, DarksteelCitadel.class})
class MawOfTheMireTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Maw of the Mire puts it on the stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Maw of the Mire");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target land and gains 4 life")
    void destroysLandAndGainsLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Maw of the Mire goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Maw of the Mire");
    }

    @Test
    @DisplayName("Fizzles and does not gain life when target land is removed before resolution")
    void fizzlesAndDoesNotGainLifeWhenTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Maw of the Mire");
    }

    @Test
    @DisplayName("Cannot target a creature with Maw of the Mire")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AmbushViper());
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID creatureId = harness.getPermanentId(player2, "Ambush Viper");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy your own land and gain life")
    void destroysOwnLandAndGainsLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(DarksteelCitadel.class)
    @DisplayName("Still gains life when the targeted land is indestructible")
    void gainsLifeEvenWhenLandCannotBeDestroyed() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel()).getId();
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Darksteel Citadel")).isEqualTo(targetId);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Maw of the Mire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land that leaves and returns is no longer the spell's target")
    void returnedLandIsNotDestroyedAndNoLifeIsGained() {
        Mountain land = new Mountain();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, land).getId();
        harness.setHand(player1, List.of(new MawOfTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, targetId);

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.setExile(player2, List.of(land));
        harness.setExile(player2, List.of());
        UUID returnedId = harness.addToBattlefieldAndReturn(player2, land).getId();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Mountain")).isEqualTo(returnedId);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Maw of the Mire");
        assertThat(gd.stack).isEmpty();
    }
}
