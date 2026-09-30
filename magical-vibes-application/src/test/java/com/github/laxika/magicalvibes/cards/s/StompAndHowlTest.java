package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.s.SealOfFire;
import com.github.laxika.magicalvibes.cards.s.SimicSignet;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StompAndHowl.class, EnchantedEvening.class, SealOfFire.class, SimicSignet.class})
class StompAndHowlTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact and a target enchantment")
    void destroysTargetArtifactAndEnchantment() {
        harness.addToBattlefield(player2, new SimicSignet());
        harness.addToBattlefield(player2, new SealOfFire());
        harness.setHand(player1, List.of(new StompAndHowl()));
        addMana();

        UUID artifactId = harness.getPermanentId(player2, "Simic Signet");
        UUID enchantmentId = harness.getPermanentId(player2, "Seal of Fire");
        harness.castAndResolveSorcery(player1, 0, List.of(artifactId, enchantmentId));

        harness.assertNotOnBattlefield(player2, "Simic Signet");
        harness.assertNotOnBattlefield(player2, "Seal of Fire");
        harness.assertInGraveyard(player2, "Simic Signet");
        harness.assertInGraveyard(player2, "Seal of Fire");
    }

    @Test
    @DisplayName("Destroys the remaining target when the other target becomes illegal")
    void destroysRemainingTargetWhenOtherTargetLeaves() {
        harness.addToBattlefield(player2, new SimicSignet());
        harness.addToBattlefield(player2, new SealOfFire());
        harness.setHand(player1, List.of(new StompAndHowl()));
        addMana();

        UUID artifactId = harness.getPermanentId(player2, "Simic Signet");
        UUID enchantmentId = harness.getPermanentId(player2, "Seal of Fire");
        harness.castSorcery(player1, 0, List.of(artifactId, enchantmentId));

        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(artifactId));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Seal of Fire");
        harness.assertInGraveyard(player2, "Seal of Fire");
    }

    @Test
    @DisplayName("Rejects an artifact in the enchantment target position")
    void rejectsWrongTargetType() {
        harness.addToBattlefield(player2, new SimicSignet());
        harness.addToBattlefield(player2, new SimicSignet());
        harness.setHand(player1, List.of(new StompAndHowl()));
        addMana();

        UUID artifactId = harness.getPermanentId(player2, "Simic Signet");
        UUID secondArtifactId = findPermanents(player2, "Simic Signet").get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId, secondArtifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows one artifact-enchantment permanent to fill both target positions")
    void allowsSharedArtifactEnchantmentTarget() {
        harness.addToBattlefield(player2, new EnchantedEvening());
        harness.addToBattlefield(player2, new SimicSignet());
        harness.setHand(player1, List.of(new StompAndHowl()));
        addMana();

        UUID signetId = harness.getPermanentId(player2, "Simic Signet");
        harness.castAndResolveSorcery(player1, 0, List.of(signetId, signetId));

        harness.assertNotOnBattlefield(player2, "Simic Signet");
        harness.assertInGraveyard(player2, "Simic Signet");
        harness.assertOnBattlefield(player2, "Enchanted Evening");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
