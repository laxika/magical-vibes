package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfJund;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicSubmersion.class, Forest.class, CylianElf.class, EtheriumSculptor.class, ObeliskOfJund.class})
class VolcanicSubmersionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        Permanent sculptor = harness.addToBattlefieldAndReturn(player2, new EtheriumSculptor());

        harness.setHand(player1, List.of(new VolcanicSubmersion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, sculptor.getId());

        harness.assertNotOnBattlefield(player2, "Etherium Sculptor");
        harness.assertInGraveyard(player2, "Etherium Sculptor");
    }

    @Test
    @DisplayName("Destroys target land")
    void destroysLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new VolcanicSubmersion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, forest.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither artifact nor land")
    void cannotTargetCreature() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new CylianElf());

        harness.setHand(player1, List.of(new VolcanicSubmersion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, elf.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new VolcanicSubmersion()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Volcanic Submersion");
        harness.assertInGraveyard(player1, "Volcanic Submersion");
        harness.assertNotInHand(player1, "Cylian Elf");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Volcanic Submersion");
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Destroys a noncreature artifact")
    void destroysNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ObeliskOfJund());
        harness.setHand(player1, List.of(new VolcanicSubmersion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Obelisk of Jund");
        harness.assertInGraveyard(player2, "Obelisk of Jund");
        harness.assertInGraveyard(player1, "Volcanic Submersion");
    }

    @Test
    @DisplayName("Can destroy its controller's land")
    void destroysOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new VolcanicSubmersion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cycling requires two mana and does not discard when payment fails")
    void cannotCycleWithInsufficientMana() {
        harness.setHand(player1, List.of(new VolcanicSubmersion()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Volcanic Submersion");
        harness.assertNotInGraveyard(player1, "Volcanic Submersion");
        harness.assertNotInHand(player1, "Cylian Elf");
        assertThat(gd.stack).isEmpty();
    }
}
