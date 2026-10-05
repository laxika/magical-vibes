package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenDisaster.class, GrizzlyBears.class, AirElemental.class, Shock.class, MoggFanatic.class})
class MoltenDisasterTest extends BaseCardTest {

    @Test
    void dealsXDamageToPlayersAndNonFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void damagesNonFlyingCreaturesOnBothSidesButSparesFlyingCreaturesEvenAtLethalX() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, 4);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Molten Disaster");
    }

    @Test
    void kickedSpellWithZeroXDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Molten Disaster");
    }

    @Test
    void unKickedSpellCanBeRespondedToWhileOnStack() {
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(harness.getGameData().stack).hasSize(2);
    }

    @Test
    void splitSecondAlsoPreventsNonManaAbilityActivation() {
        addCreatureReady(player2, new MoggFanatic());
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castKickedSorcery(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedSpellHasSplitSecondWhileOnStack() {
        harness.setHand(player1, List.of(new MoltenDisaster()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castKickedSorcery(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        assertThat(harness.getGameData().stack).hasSize(1);
    }
}
