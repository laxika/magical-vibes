package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.t.TerashisCry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sideswipe.class, GlacialRay.class, TerashisCry.class, LanternKami.class})
class SideswipeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the choice moves the Arcane spell's target")
    void retargetsArcaneSpell() {
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        GlacialRay ray = new GlacialRay();
        harness.setHand(player1, List.of(ray));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bears2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ray.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, bears1.getId());

        StackEntry rayEntry = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Glacial Ray"))
                .findFirst().orElseThrow();
        assertThat(rayEntry.getTargetId()).isEqualTo(bears1.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lantern Kami");
        harness.assertOnBattlefield(player2, "Lantern Kami");
    }

    @Test
    @DisplayName("Retargets a multi-target Arcane spell with one chosen target")
    void retargetsChosenTargetOfMultiTargetArcaneSpell() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        Permanent replacementTarget = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        TerashisCry cry = new TerashisCry();
        harness.setHand(player1, List.of(cry));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(originalTarget.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, cry.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, replacementTarget.getId());

        StackEntry cryEntry = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Terashi's Cry"))
                .findFirst().orElseThrow();
        assertThat(cryEntry.getDeclaredTargetIds()).containsExactly(replacementTarget.getId());

        harness.passBothPriorities();

        assertThat(originalTarget.isTapped()).isFalse();
        assertThat(replacementTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not offer the Arcane spell's current target as a replacement")
    void doesNotOfferCurrentTargetAsReplacement() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        Permanent replacementTarget = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        GlacialRay ray = new GlacialRay();
        harness.setHand(player1, List.of(ray));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ray.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player2, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(replacementTarget.getId()).doesNotContain(originalTarget.getId());

        harness.handlePermanentChosen(player2, replacementTarget.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining leaves the Arcane spell's original target")
    void decliningKeepsOriginalTarget() {
        harness.addToBattlefield(player1, new LanternKami());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        GlacialRay ray = new GlacialRay();
        harness.setHand(player1, List.of(ray));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, bears2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ray.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lantern Kami");
        harness.assertOnBattlefield(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Cannot target a non-Arcane spell")
    void cannotTargetNonArcaneSpell() {
        LanternKami creature = new LanternKami();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Sideswipe()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
