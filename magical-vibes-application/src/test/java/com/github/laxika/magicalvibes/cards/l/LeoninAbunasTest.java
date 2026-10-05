package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninAbunas.class, AetherSpellbomb.class, AlphaMyr.class, Atog.class, Shatter.class})
class LeoninAbunasTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts and artifact creatures you control have hexproof")
    void grantsHexproofToOwnArtifacts() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not grant hexproof to nonartifacts or opponents' artifacts")
    void limitsHexproofToOwnArtifacts() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent nonartifact = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new AetherSpellbomb());

        assertThat(gqs.hasKeyword(gd, nonartifact, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target an artifact you control")
    void opponentCannotTargetOwnArtifact() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact controller can target their own artifact")
    void controllerCanTargetOwnArtifact() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent's activated ability cannot target a protected artifact creature")
    void opponentAbilityCannotTargetArtifactCreature() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player2, "Aether Spellbomb");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Leonin Abunas to hand ends its protection")
    void protectionEndsWhenSourceLeaves() {
        Permanent abunas = harness.addToBattlefieldAndReturn(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, 0, null, abunas.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Abunas");
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isFalse();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Alpha Myr");
    }

    @Test
    @DisplayName("A spell fails to resolve if its artifact target gains hexproof before resolution")
    void protectionIsCheckedAgainAtResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, artifact.getId());

        harness.enterBattlefieldAndReturn(player1, new LeoninAbunas());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aether Spellbomb");
        harness.assertInGraveyard(player2, "Shatter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Artifacts controlled by the opponent remain vulnerable")
    void opponentArtifactCanBeDestroyed() {
        harness.addToBattlefield(player1, new LeoninAbunas());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AetherSpellbomb());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Aether Spellbomb");
        harness.assertInGraveyard(player2, "Aether Spellbomb");
    }
}
