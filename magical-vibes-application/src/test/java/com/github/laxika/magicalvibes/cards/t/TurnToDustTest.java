package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurnToDust.class, LeoninScimitar.class, AlphaMyr.class})
class TurnToDustTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Turn to Dust destroys target Equipment and adds green mana")
    void destroysEquipmentAndAddsMana() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new TurnToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, equipment.getId());

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Turn to Dust cannot target a non-Equipment artifact creature")
    void cannotTargetNonEquipmentArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new TurnToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Turn to Dust adds green mana even when the target Equipment is indestructible")
    void addsManaWhenEquipmentIsIndestructible() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new TurnToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, equipment.getId());

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertNotInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Turn to Dust fizzles when its target leaves before resolution")
    void fizzlesWhenTargetLeaves() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new TurnToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, equipment.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Turn to Dust");
    }

    @Test
    @DisplayName("Turn to Dust can destroy its controller's attached Equipment without destroying the creature")
    void destroysOwnAttachedEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new TurnToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, equipment.getId());

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertNotInGraveyard(player1, "Alpha Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Turn to Dust adds no mana when opposing Equipment gains hexproof before resolution")
    void addsNoManaWhenTargetGainsHexproof() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new TurnToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, equipment.getId());
        equipment.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertNotInGraveyard(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Turn to Dust");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
