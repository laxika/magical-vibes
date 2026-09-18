package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenRain.class, Plains.class, GreatFurnace.class, AlphaMyr.class, DarksteelCitadel.class})
class MoltenRainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a basic land without dealing damage")
    void destroysBasicLandWithoutDamage() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new MoltenRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player2, "Plains");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroys a nonbasic land and deals 2 damage to its controller")
    void destroysNonbasicLandAndDealsDamage() {
        harness.addToBattlefield(player2, new GreatFurnace());
        harness.setHand(player1, List.of(new MoltenRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Great Furnace");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Great Furnace");
        harness.assertInGraveyard(player2, "Great Furnace");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fizzles if the target land leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GreatFurnace());
        harness.setHand(player1, List.of(new MoltenRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Great Furnace");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new MoltenRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Alpha Myr");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals damage when an indestructible nonbasic land survives")
    void dealsDamageWhenIndestructibleNonbasicLandSurvives() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new MoltenRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Citadel");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertLife(player2, 18);
    }
}
