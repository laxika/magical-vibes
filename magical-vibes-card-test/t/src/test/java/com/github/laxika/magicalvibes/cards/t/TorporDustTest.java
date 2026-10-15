package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElsewhereFlask;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorporDust.class, SafeholdElite.class, ElsewhereFlask.class})
class TorporDustTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Torpor Dust targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());

        harness.setHand(player1, List.of(new TorporDust()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Torpor Dust");
    }

    @Test
    @DisplayName("Resolving Torpor Dust attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());

        harness.setHand(player1, List.of(new TorporDust()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Torpor Dust")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets -3/-0")
    void enchantedCreatureGetsDebuff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TorporDust());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to base stats when Torpor Dust is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TorporDust());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Torpor Dust")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new SafeholdElite());
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.setHand(player1, List.of(new TorporDust()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent artifact = findPermanent(player1, "Elsewhere Flask");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Flash allows enchanting an opponent's creature during their upkeep")
    void flashDuringOpponentsUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        harness.setHand(player1, List.of(new TorporDust()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Torpor Dust").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Safehold Elite");
    }

    @Test
    @DisplayName("Torpor Dust goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        harness.setHand(player1, List.of(new TorporDust()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setExile(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Torpor Dust");
        harness.assertInGraveyard(player1, "Torpor Dust");
    }

    @Test
    @DisplayName("Multiple copies of Torpor Dust each reduce the enchanted creature's power")
    void multipleAurasStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        harness.setHand(player1, List.of(new TorporDust(), new TorporDust()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Torpor Dust")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }
}
