package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BarbedSextant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingsOfAesthir.class, BalduvianBears.class, BarbedSextant.class})
class WingsOfAesthirTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wings of Aesthir attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new WingsOfAesthir()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wings of Aesthir")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and has flying and first strike")
    void enchantedCreatureGetsBoostAndKeywords() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingsOfAesthir());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the boost and keywords when the Aura leaves")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingsOfAesthir());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Wings of Aesthir does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent otherBears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingsOfAesthir());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Wings of Aesthir can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new WingsOfAesthir()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wings of Aesthir")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Wings of Aesthir fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new WingsOfAesthir()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wings of Aesthir");
        harness.assertNotOnBattlefield(player1, "Wings of Aesthir");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BarbedSextant());
        harness.setHand(player1, List.of(new WingsOfAesthir()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
