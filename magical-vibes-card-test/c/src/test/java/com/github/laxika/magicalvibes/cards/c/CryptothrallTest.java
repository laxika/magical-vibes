package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cryptothrall.class, AlphaMyr.class, AetherSpellbomb.class, Atog.class})
class CryptothrallTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other artifact creatures you control hexproof")
    void givesOtherArtifactCreaturesYouControlHexproof() {
        Permanent cryptothrall = harness.addToBattlefieldAndReturn(player1, new Cryptothrall());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, cryptothrall, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Two Cryptothralls grant each other hexproof")
    void twoCryptothrallsProtectEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Cryptothrall());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Cryptothrall());

        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof prevents opponents from targeting protected creatures")
    void opponentCannotTargetProtectedArtifactCreature() {
        harness.addToBattlefield(player1, new Cryptothrall());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, protectedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertOnBattlefield(player2, "Aether Spellbomb");
    }

    @Test
    @DisplayName("Hexproof permits the controller to target protected creatures")
    void controllerCanTargetProtectedArtifactCreature() {
        harness.addToBattlefield(player1, new Cryptothrall());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 2, null, protectedCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        harness.assertInHand(player1, "Alpha Myr");
    }

    @Test
    @DisplayName("Protection ends immediately when Cryptothrall leaves the battlefield")
    void protectionEndsWhenCryptothrallLeaves() {
        Permanent cryptothrall = harness.addToBattlefieldAndReturn(player1, new Cryptothrall());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.addMana(player2, ManaColor.BLUE, 1);
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isTrue();

        harness.activateAbility(player2, 0, null, cryptothrall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cryptothrall");
        harness.assertInHand(player1, "Cryptothrall");
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isFalse();
    }
}
