package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({GiftOfFangs.class, HillGiant.class, VampireInterloper.class, FountainOfYouth.class})
class GiftOfFangsTest extends BaseCardTest {

    @Test
    @DisplayName("Gift of Fangs gives a Vampire +2/+2")
    void vampireGetsBoost() {
        Permanent vampire = new Permanent(new VampireInterloper());
        gd.playerBattlefields.get(player1.getId()).add(vampire);

        Permanent aura = new Permanent(new GiftOfFangs());
        aura.setAttachedTo(vampire.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gift of Fangs gives a non-Vampire -2/-2")
    void nonVampireGetsDebuff() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);

        Permanent aura = new Permanent(new GiftOfFangs());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving Gift of Fangs attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(giant);

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
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiftOfFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
