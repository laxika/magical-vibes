package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AltarOfDementia;
import com.github.laxika.magicalvibes.cards.a.Aluren;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disenchant.class, AltarOfDementia.class, BottleGnomes.class, Aluren.class,
        SoltariFootSoldier.class})
class DisenchantTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesAndDestroysArtifact() {
        harness.addToBattlefield(player2, new AltarOfDementia());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Altar of Dementia");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Altar of Dementia");
        harness.assertInGraveyard(player2, "Altar of Dementia");
    }

    @Test
    @DisplayName("Resolving can destroy an artifact you control")
    void resolvesAndDestroysOwnArtifact() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Altar of Dementia");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Altar of Dementia");
        harness.assertInGraveyard(player1, "Altar of Dementia");
    }

    @Test
    @DisplayName("Resolving destroys target artifact creature")
    void resolvesAndDestroysArtifactCreature() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Bottle Gnomes");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new Aluren());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Aluren");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Aluren");
        harness.assertInGraveyard(player2, "Aluren");
    }

    @Test
    @DisplayName("Cannot target creature with Disenchant")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new SoltariFootSoldier());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Soltari Foot Soldier");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
