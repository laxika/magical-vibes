package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfFangs.class, HillGiant.class, VampireInterloper.class, FountainOfYouth.class, PersistentSpecimen.class})
class GiftOfFangsTest extends BaseCardTest {

    @Test
    @DisplayName("Gift of Fangs gives a Vampire +2/+2")
    void vampireGetsBoost() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireInterloper());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiftOfFangs());
        aura.setAttachedTo(vampire.getId());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gift of Fangs gives a non-Vampire -2/-2")
    void nonVampireGetsDebuff() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiftOfFangs());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving Gift of Fangs attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new GiftOfFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gift of Fangs")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Gift of Fangs cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiftOfFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Gift of Fangs sends a non-Vampire with lethal toughness reduction and the Aura to their owners' graveyards")
    void lethalToughnessReductionPutsCreatureAndAuraInGraveyards() {
        Permanent specimen = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        harness.setHand(player1, List.of(new GiftOfFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, specimen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(specimen);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Persistent Specimen");
        harness.assertInGraveyard(player1, "Gift of Fangs");
    }

    @Test
    @DisplayName("Gift of Fangs does not enter the battlefield when its target leaves before resolution")
    void missingTargetPreventsAuraFromEntering() {
        Permanent specimen = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        harness.setHand(player1, List.of(new GiftOfFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, specimen.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, specimen));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Gift of Fangs");
        harness.assertInGraveyard(player2, "Persistent Specimen");
    }

    @Test
    @DisplayName("Removing Gift of Fangs removes its Vampire boost")
    void removingAuraRemovesBoost() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new GiftOfFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, vampire.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
        Permanent aura = findPermanent(player1, "Gift of Fangs");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Gift of Fangs");
    }
}
