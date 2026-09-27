package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.h.HondenOfCleansingFire;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuietPurity.class, HondenOfCleansingFire.class, SenseisDiviningTop.class,
        WanderingOnes.class})
class QuietPurityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new HondenOfCleansingFire());
        harness.setHand(player1, List.of(new QuietPurity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Honden of Cleansing Fire");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Honden of Cleansing Fire");
        harness.assertInGraveyard(player2, "Honden of Cleansing Fire");
    }

    @Test
    @DisplayName("Can target an enchantment controlled by the caster")
    void resolvesAgainstOwnEnchantment() {
        harness.addToBattlefield(player1, new HondenOfCleansingFire());
        harness.setHand(player1, List.of(new QuietPurity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Honden of Cleansing Fire");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Honden of Cleansing Fire");
        harness.assertInGraveyard(player1, "Honden of Cleansing Fire");
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        harness.addToBattlefield(player2, new SenseisDiviningTop());
        harness.setHand(player1, List.of(new QuietPurity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Sensei's Divining Top");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new WanderingOnes());
        harness.setHand(player1, List.of(new QuietPurity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Wandering Ones");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
