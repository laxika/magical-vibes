package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.e.ErrantEphemeron;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OpalGuardian;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheDisk.class, ChromaticStar.class, OpalGuardian.class, Forest.class, ErrantEphemeron.class,
        Snapback.class})
class MagusOfTheDiskTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts, creatures, and enchantments but not lands")
    void destroysArtifactsCreaturesAndEnchantments() {
        addCreatureReady(player1, new MagusOfTheDisk());
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.addToBattlefield(player1, new OpalGuardian());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new ErrantEphemeron());
        harness.addToBattlefield(player2, new Forest());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Magus of the Disk");
        harness.assertNotOnBattlefield(player1, "Chromatic Star");
        harness.assertNotOnBattlefield(player1, "Opal Guardian");
        harness.assertNotOnBattlefield(player2, "Errant Ephemeron");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Magus of the Disk enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new MagusOfTheDisk(), "{2}{W}{W}");
        harness.passBothPriorities();

        Permanent magus = findPermanent(player1, "Magus of the Disk");
        assertThat(magus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new MagusOfTheDisk());
        findPermanent(player1, "Magus of the Disk").untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability without paying its generic mana cost")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new MagusOfTheDisk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped even after summoning sickness ends")
    void cannotActivateWhileTapped() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheDisk());
        magus.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation taps Magus immediately and destruction waits for resolution")
    void paysTapCostBeforeResolution() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheDisk());
        harness.addToBattlefield(player2, new ErrantEphemeron());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(magus.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Magus of the Disk");
        harness.assertOnBattlefield(player2, "Errant Ephemeron");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magus of the Disk");
        harness.assertInGraveyard(player2, "Errant Ephemeron");
    }

    @Test
    @DisplayName("The destruction ability resolves after Magus is returned to hand")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheDisk());
        harness.addToBattlefield(player2, new ErrantEphemeron());
        harness.addToBattlefield(player2, new OpalGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, magus.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Magus of the Disk");
        harness.assertOnBattlefield(player2, "Errant Ephemeron");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Magus of the Disk");
        harness.assertNotInGraveyard(player1, "Magus of the Disk");
        harness.assertInGraveyard(player2, "Errant Ephemeron");
        harness.assertInGraveyard(player2, "Opal Guardian");
    }
}
