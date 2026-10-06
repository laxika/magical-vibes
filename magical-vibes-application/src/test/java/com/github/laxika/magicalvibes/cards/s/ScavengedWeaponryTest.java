package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PhyrexianAltar;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengedWeaponry.class, NomadicElf.class, PhyrexianAltar.class})
class ScavengedWeaponryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Scavenged Weaponry attaches it and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        harness.setHand(player1, List.of(new ScavengedWeaponry()));
        harness.setLibrary(player1, List.of(new NomadicElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ScavengedWeaponry
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Scavenged Weaponry can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player1, List.of(new ScavengedWeaponry()));
        harness.setLibrary(player1, List.of(new NomadicElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ScavengedWeaponry
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ScavengedWeaponry());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost ends when Scavenged Weaponry leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ScavengedWeaponry());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Scavenged Weaponry")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PhyrexianAltar());
        harness.setHand(player1, List.of(new ScavengedWeaponry()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Only the enchanted creature gets the bonus")
    void onlyEnchantedCreatureGetsBoost() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ScavengedWeaponry());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("No card is drawn when the Aura's target leaves before resolution")
    void missingTargetPreventsEntryAndDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        harness.setHand(player1, List.of(new ScavengedWeaponry()));
        harness.setLibrary(player1, List.of(new NomadicElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Scavenged Weaponry");
        harness.assertInGraveyard(player1, "Scavenged Weaponry");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw trigger still resolves after the Aura leaves")
    void drawTriggerSurvivesAuraLeaving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player1, List.of(new ScavengedWeaponry()));
        harness.setLibrary(player1, List.of(new NomadicElf()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent aura = findPermanent(player1, "Scavenged Weaponry");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        resolveAllTriggers();

        harness.assertInHand(player1, "Nomadic Elf");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
