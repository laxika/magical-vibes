package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.w.WallOfFaith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoonOfEmrakulTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Boon of Emrakul attaches it and changes the enchanted creature's stats")
    void resolvingAttachesAndChangesStats() {
        Permanent wall = new Permanent(new WallOfFaith());
        gd.playerBattlefields.get(player1.getId()).add(wall);
        harness.setHand(player1, List.of(new BoonOfEmrakul()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BoonOfEmrakul
                        && wall.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
    }

    @Test
    @DisplayName("The stat change ends when Boon of Emrakul leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent wall = new Permanent(new WallOfFaith());
        gd.playerBattlefields.get(player1.getId()).add(wall);
        Permanent aura = new Permanent(new BoonOfEmrakul());
        aura.setAttachedTo(wall.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boon of Emrakul fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent wall = new Permanent(new WallOfFaith());
        gd.playerBattlefields.get(player1.getId()).add(wall);
        harness.setHand(player1, List.of(new BoonOfEmrakul()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, wall.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boon of Emrakul");
        harness.assertNotOnBattlefield(player1, "Boon of Emrakul");
    }

    @Test
    @DisplayName("Boon of Emrakul cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BoonOfEmrakul()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
