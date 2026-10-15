package com.github.laxika.magicalvibes.cards.g;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.b.BorosCharm;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@CardUsed({GrislySpectacle.class, EliteVanguard.class, IronMyr.class, BorosCharm.class})
class GrislySpectacleTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonartifact creature and mills its controller by its power")
    void destroysCreatureAndMillsItsControllerByPower() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        UUID targetId = harness.getPermanentId(player2, "Elite Vanguard");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new IronMyr());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Iron Myr");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact creature");
    }

    @Test
    @DisplayName("Fizzles without milling when the target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        UUID targetId = harness.getPermanentId(player2, "Elite Vanguard");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 5})
    @DisplayName("Uses the creature's modified power at resolution, with no mill for nonpositive power")
    void millsByPowerAtResolution(int power) {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, creature.getId());
        creature.setPowerModifier(power - 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Elite Vanguard");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - Math.max(0, power));
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1 + Math.max(0, power));
    }

    @Test
    @DisplayName("Can destroy your own creature and mills you rather than your opponent")
    void canTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int ownLibraryBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownLibraryBefore - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mills only the available cards when the library is smaller than the creature's power")
    void millsRemainingLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        GrislySpectacle libraryCard = new GrislySpectacle();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(creature.getCard(), libraryCard);
    }

    @Test
    @DisplayName("Still mills the controller of a creature protected by Boros Charm")
    void millsEvenWhenCreatureIsIndestructible() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        harness.setHand(player2, List.of(new BorosCharm()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, 1, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Elite Vanguard");
        harness.assertNotInGraveyard(player2, "Elite Vanguard");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
