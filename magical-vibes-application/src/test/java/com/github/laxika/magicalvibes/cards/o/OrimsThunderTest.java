package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.f.FerventCharge;
import com.github.laxika.magicalvibes.cards.p.PenumbraWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrimsThunder.class, Dodecapod.class, FerventCharge.class, PenumbraWurm.class})
class OrimsThunderTest extends BaseCardTest {

    private void addOrimsThunderMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Without kicker, destroys the artifact without needing a creature target")
    void destroysArtifactWithoutKicker() {
        harness.addToBattlefield(player2, new Dodecapod());
        harness.addToBattlefield(player2, new PenumbraWurm());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Dodecapod");
        harness.castInstant(player1, 0, artifactId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dodecapod");
        assertThat(findPermanent(player2, "Penumbra Wurm").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Without kicker, destroys the enchantment")
    void destroysEnchantmentWithoutKicker() {
        harness.addToBattlefield(player2, new FerventCharge());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID enchantmentId = harness.getPermanentId(player2, "Fervent Charge");
        harness.castInstant(player1, 0, enchantmentId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fervent Charge");
    }

    @Test
    @DisplayName("When kicked, cannot use an enchantment as the creature target")
    void kickedCannotTargetEnchantmentForDamage() {
        harness.addToBattlefield(player2, new Dodecapod());
        harness.addToBattlefield(player2, new FerventCharge());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Dodecapod");
        UUID enchantmentId = harness.getPermanentId(player2, "Fervent Charge");
        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(
                player1, 0, artifactId, List.of(enchantmentId), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When kicked, deals damage equal to the destroyed artifact's mana value")
    void kickedDealsArtifactManaValueDamageToCreature() {
        harness.addToBattlefield(player2, new Dodecapod());
        harness.addToBattlefield(player2, new PenumbraWurm());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Dodecapod");
        UUID creatureId = harness.getPermanentId(player2, "Penumbra Wurm");
        harness.castKickedInstantWithSacrifices(player1, 0, artifactId, List.of(creatureId), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dodecapod");
        harness.assertNotInGraveyard(player2, "Penumbra Wurm");
        assertThat(findPermanent(player2, "Penumbra Wurm").getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("If the artifact target is illegal, the kicked damage is not dealt")
    void kickedDoesNotDamageWhenArtifactTargetIsRemoved() {
        harness.addToBattlefield(player2, new Dodecapod());
        harness.addToBattlefield(player2, new PenumbraWurm());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Dodecapod");
        UUID creatureId = harness.getPermanentId(player2, "Penumbra Wurm");
        harness.castKickedInstantWithSacrifices(player1, 0, artifactId, List.of(creatureId), List.of());
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(artifactId));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Penumbra Wurm").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("If the creature target is illegal, still destroys the artifact")
    void kickedStillDestroysWhenCreatureTargetIsRemoved() {
        harness.addToBattlefield(player2, new Dodecapod());
        harness.addToBattlefield(player2, new PenumbraWurm());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Dodecapod");
        UUID creatureId = harness.getPermanentId(player2, "Penumbra Wurm");
        harness.castKickedInstantWithSacrifices(player1, 0, artifactId, List.of(creatureId), List.of());
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(creatureId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dodecapod");
    }

    @Test
    @DisplayName("Cannot target a creature for the artifact or enchantment target")
    void cannotTargetCreatureAsPermanentTarget() {
        harness.addToBattlefield(player2, new PenumbraWurm());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID creatureId = harness.getPermanentId(player2, "Penumbra Wurm");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
