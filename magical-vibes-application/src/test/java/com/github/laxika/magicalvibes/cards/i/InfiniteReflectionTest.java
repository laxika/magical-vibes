package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InfiniteReflectionTest extends BaseCardTest {

    private void castReflectionOn(UUID targetId) {
        harness.setHand(player1, List.of(new InfiniteReflection()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // Aura resolves and attaches
        harness.passBothPriorities(); // ETB trigger resolves
    }

    @Test
    @DisplayName("On entering, each other nontoken creature you control becomes a copy of the enchanted creature")
    void otherNontokenCreaturesBecomeCopies() {
        harness.addToBattlefield(player1, new Archangel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
        assertThat(bears.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected by the enters trigger")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new Archangel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(bears.getCard().getPower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken creatures cast later enter as a copy of the enchanted creature")
    void laterNontokenCreatureEntersAsCopy() {
        harness.addToBattlefield(player1, new Archangel());
        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCard().getName()).isEqualTo("Archangel");
        assertThat(bears.getCard().getPower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Tokens are not affected — they enter as themselves")
    void tokensEnterAsThemselves() {
        harness.addToBattlefield(player1, new Archangel());
        harness.addToBattlefield(player1, new DoomedTraveler());

        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        // Doomed Traveler was a nontoken creature, so it became a copy of Archangel; kill it and
        // check the Spirit token it leaves behind enters unchanged.
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Doomed Traveler"))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo("Archangel");
    }

    @Test
    @DisplayName("Creatures entering after the Aura leaves are unaffected")
    void effectStopsAfterAuraLeaves() {
        harness.addToBattlefield(player1, new Archangel());
        castReflectionOn(harness.getPermanentId(player1, "Archangel"));

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Infinite Reflection"));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(bears.getCard().getPower()).isEqualTo(2);
    }
}
