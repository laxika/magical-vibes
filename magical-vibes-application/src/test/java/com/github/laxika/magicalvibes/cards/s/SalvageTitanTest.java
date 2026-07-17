package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalvageTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by sacrificing three artifacts instead of paying mana")
    void castBySacrificingThreeArtifacts() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Juggernaut());
        harness.addToBattlefield(player1, new CopperMyr());

        UUID ornithopter = harness.getPermanentId(player1, "Ornithopter");
        UUID juggernaut = harness.getPermanentId(player1, "Juggernaut");
        UUID copperMyr = harness.getPermanentId(player1, "Copper Myr");

        harness.setHand(player1, List.of(new SalvageTitan()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreatureWithAlternateCost(player1, 0, List.of(ornithopter, juggernaut, copperMyr));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Salvage Titan"))
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"))
                .noneMatch(p -> p.getCard().getName().equals("Juggernaut"))
                .noneMatch(p -> p.getCard().getName().equals("Copper Myr"));

        // The mana was not spent — the alternate cost was paid instead.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }

    @Test
    @DisplayName("Graveyard ability returns Salvage Titan to hand, exiling three artifact cards")
    void graveyardAbilityReturnsSelf() {
        SalvageTitan titan = new SalvageTitan();
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Juggernaut(), new CopperMyr(), titan));

        harness.activateGraveyardAbility(player1, 3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Salvage Titan"));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"))
                .anyMatch(c -> c.getName().equals("Juggernaut"))
                .anyMatch(c -> c.getName().equals("Copper Myr"))
                .noneMatch(c -> c.getName().equals("Salvage Titan"));
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated without three other artifact cards to exile")
    void graveyardAbilityRequiresThreeArtifacts() {
        SalvageTitan titan = new SalvageTitan();
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Juggernaut(), titan));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Salvage Titan"));
    }
}
