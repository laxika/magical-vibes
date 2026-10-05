package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IndomitableArchangel.class, Memnite.class, AccordersShield.class, GoldMyr.class, AlphaTyrranax.class, Shatter.class})
class IndomitableArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Without metalcraft, artifacts do not have shroud")
    void noShroudWithoutMetalcraft() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());

        assertThat(gqs.hasKeyword(gd, memnite, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("With only two artifacts, artifacts do not have shroud")
    void noShroudWithTwoArtifacts() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.hasKeyword(gd, memnite, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("With metalcraft, artifacts you control have shroud")
    void artifactsHaveShroudWithMetalcraft() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new GoldMyr());

        assertThat(gqs.hasKeyword(gd, memnite, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, shield, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, myr, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Indomitable Archangel itself does not get shroud (not an artifact)")
    void archangelDoesNotGetShroud() {
        Permanent archangel = harness.addToBattlefieldAndReturn(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new GoldMyr());

        assertThat(gqs.hasKeyword(gd, archangel, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Non-artifact creatures do not get shroud")
    void nonArtifactCreatureDoesNotGetShroud() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new GoldMyr());
        Permanent tyrranax = harness.addToBattlefieldAndReturn(player1, new AlphaTyrranax());

        assertThat(gqs.hasKeyword(gd, tyrranax, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifacts do not get shroud")
    void opponentArtifactsDoNotGetShroud() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new GoldMyr());

        Permanent opponentMemnite = harness.addToBattlefieldAndReturn(player2, new Memnite());

        assertThat(gqs.hasKeyword(gd, opponentMemnite, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud is lost when artifact count drops below three")
    void shroudLostWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new GoldMyr());

        // With 3 artifacts, has shroud
        assertThat(gqs.hasKeyword(gd, memnite, Keyword.SHROUD)).isTrue();

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Gold Myr"));

        assertThat(gqs.hasKeyword(gd, memnite, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCountForMetalcraft() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        // Only 1 artifact controlled by player1; 2 more on opponent's side
        harness.addToBattlefield(player2, new AccordersShield());
        harness.addToBattlefield(player2, new GoldMyr());

        assertThat(gqs.hasKeyword(gd, memnite, Keyword.SHROUD)).isFalse();
    }

    @Test
    void shroudPreventsBothPlayersFromTargetingArtifacts() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setHand(player1, List.of(new Shatter()));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("shroud");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("shroud");
    }

    @Test
    void spellOnStackLosesItsTargetWhenMetalcraftBecomesActive() {
        harness.addToBattlefield(player1, new IndomitableArchangel());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, artifact.getId());

        harness.addToBattlefield(player1, new GoldMyr());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player2, "Shatter");
    }

    @Test
    void artifactsCanBeTargetedAfterArchangelLeaves() {
        Permanent archangel = harness.addToBattlefieldAndReturn(player1, new IndomitableArchangel());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new GoldMyr());
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.SHROUD)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, archangel));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player1, "Memnite");
    }
}
