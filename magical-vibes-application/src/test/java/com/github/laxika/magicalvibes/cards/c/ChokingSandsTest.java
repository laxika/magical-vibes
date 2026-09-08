package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChokingSands.class, CrystalVein.class, Plains.class, Swamp.class, ZhalfirinKnight.class})
class ChokingSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a basic non-Swamp land without dealing damage")
    void destroysBasicLandNoDamage() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player2, "Plains");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Destroys a nonbasic land and deals 2 damage to its controller")
    void destroysNonbasicLandAndDealsDamage() {
        harness.addToBattlefield(player2, new CrystalVein());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Crystal Vein");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Crystal Vein");
        harness.assertInGraveyard(player2, "Crystal Vein");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a Swamp")
    void cannotTargetSwamp() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID swampId = harness.getPermanentId(player2, "Swamp");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, swampId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Swamp land");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player2, new ZhalfirinKnight());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Zhalfirin Knight");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Swamp land");
    }

    @Test
    @DisplayName("Fizzles if the target land leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new CrystalVein());
        harness.setHand(player1, List.of(new ChokingSands()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(player2, "Crystal Vein");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }
}
