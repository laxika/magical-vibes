package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrarkClanStoker.class, DarksteelBrute.class, CrazedGoblin.class})
class KrarkClanStokerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing an artifact adds two red mana immediately")
    void sacrificingArtifactAddsTwoRedMana() {
        addCreatureReady(player1, new KrarkClanStoker());
        harness.addToBattlefield(player1, new DarksteelBrute());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Krark-Clan Stoker");
        harness.assertInGraveyard(player1, "Darksteel Brute");
        assertThat(findPermanent(player1, "Krark-Clan Stoker").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addCreatureReady(player1, new KrarkClanStoker());
        harness.addToBattlefield(player1, new CrazedGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Cannot sacrifice an artifact controlled by an opponent")
    void cannotActivateWithOnlyOpponentsArtifact() {
        addCreatureReady(player1, new KrarkClanStoker());
        harness.addToBattlefield(player2, new DarksteelBrute());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Darksteel Brute");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new KrarkClanStoker());
        harness.addToBattlefield(player1, new DarksteelBrute());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        Permanent stoker = addCreatureReady(player1, new KrarkClanStoker());
        stoker.tap();
        harness.addToBattlefield(player1, new DarksteelBrute());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertOnBattlefield(player1, "Darksteel Brute");
    }

    @Test
    @DisplayName("A tapped indestructible artifact can be sacrificed for mana")
    void canSacrificeTappedArtifact() {
        addCreatureReady(player1, new KrarkClanStoker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute());
        artifact.tap();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Darksteel Brute");
        harness.assertInGraveyard(player1, "Darksteel Brute");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With multiple artifacts only the chosen artifact is sacrificed")
    void sacrificesOnlyChosenArtifact() {
        Permanent stoker = addCreatureReady(player1, new KrarkClanStoker());
        Permanent retained = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(stoker, retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sacrificed.getCard());
        assertThat(stoker.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
