package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DawnToDusk.class, GloriousAnthem.class, GrizzlyBears.class, Pacifism.class})
class DawnToDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 returns an enchantment card from the graveyard to hand")
    void returnsEnchantmentFromGraveyard() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0, pacifism.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Mode 0 cannot target a non-enchantment card")
    void mode0ExcludesNonEnchantments() {
        Pacifism pacifism = new Pacifism();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(pacifism, bears));
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 1 destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 1, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Mode 1 cannot target a creature")
    void cannotDestroyCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 returns an enchantment and destroys an enchantment")
    void returnsAndDestroysEnchantment() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 2,
                List.of(pacifism.getId(), harness.getPermanentId(player2, "Glorious Anthem")));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The return mode cannot target an opponent's enchantment card")
    void cannotReturnOpponentsEnchantment() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player2, List.of(pacifism));
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, pacifism.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destroy mode can target your own enchantment")
    void canDestroyOwnEnchantment() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 1, harness.getPermanentId(player1, "Glorious Anthem"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Both modes still destroy the enchantment when the graveyard target disappears")
    void destroysWhenGraveyardTargetDisappears() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 2,
                List.of(pacifism.getId(), harness.getPermanentId(player2, "Glorious Anthem")));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(pacifism));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Both modes still return the enchantment when the battlefield target disappears")
    void returnsWhenBattlefieldTargetDisappears() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DawnToDusk()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 2,
                List.of(pacifism.getId(), harness.getPermanentId(player2, "Glorious Anthem")));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Dawn to Dusk");
    }
}
