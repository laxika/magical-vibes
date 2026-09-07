package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StupefyingTouch.class, BottleGnomes.class, FountainOfYouth.class, GrizzlyBears.class})
class StupefyingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Stupefying Touch enters attached to a creature and draws a card")
    void entersAttachedAndDrawsCard() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StupefyingTouch()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Stupefying Touch")
                        && creature.getId().equals(permanent.getAttachedTo()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = new Permanent(new BottleGnomes());
        gnomes.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gnomes);

        Permanent aura = new Permanent(new StupefyingTouch());
        aura.setAttachedTo(gnomes.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Stupefying Touch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new StupefyingTouch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
