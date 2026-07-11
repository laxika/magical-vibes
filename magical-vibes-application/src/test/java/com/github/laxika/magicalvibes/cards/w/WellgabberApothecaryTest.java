package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WellgabberApothecaryTest extends BaseCardTest {

    private Permanent addTappedKnight() {
        harness.addToBattlefield(player1, new KnightOfMeadowgrain());
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Meadowgrain"))
                .findFirst().orElseThrow();
        knight.tap();
        return knight;
    }

    @Test
    @DisplayName("Prevents all damage dealt to the targeted tapped Kithkin creature this turn")
    void preventsDamage() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        Permanent knight = addTappedKnight();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        // Shock the protected 2/2 — all damage should be prevented, so it survives.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Knight of Meadowgrain"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Knight of Meadowgrain"));
    }

    @Test
    @DisplayName("Cannot target an untapped Kithkin creature")
    void rejectsUntappedTarget() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        harness.addToBattlefield(player1, new KnightOfMeadowgrain());
        UUID untappedKnight = harness.getPermanentId(player1, "Knight of Meadowgrain");

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, untappedKnight))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a tapped creature that is neither Merfolk nor Kithkin")
    void rejectsWrongSubtype() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bears.tap();

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
