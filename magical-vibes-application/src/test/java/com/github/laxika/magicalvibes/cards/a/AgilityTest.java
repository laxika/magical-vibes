package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BadRiver;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Agility.class, BadRiver.class, FemerefScouts.class})
class AgilityTest extends BaseCardTest {

    private Permanent enchantedScouts() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Agility());
        aura.setAttachedTo(scouts.getId());
        return scouts;
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has flanking")
    void enchantedCreatureBoostedAndHasFlanking() {
        Permanent scouts = enchantedScouts();

        assertThat(gqs.getEffectivePower(gd, scouts)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, scouts, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flanking end when the Aura leaves the battlefield")
    void effectsEndWhenAuraLeaves() {
        Permanent scouts = enchantedScouts();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof Agility);

        assertThat(gqs.getEffectivePower(gd, scouts)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, scouts, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent scouts = addCreatureReady(player2, new FemerefScouts());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Agility()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scouts)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, scouts, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent attacker = enchantedScouts();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new BadRiver());
        harness.setHand(player1, List.of(new Agility()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent land = findPermanent(player1, "Bad River");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
