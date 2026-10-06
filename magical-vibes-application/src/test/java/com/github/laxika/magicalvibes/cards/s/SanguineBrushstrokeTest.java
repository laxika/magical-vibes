package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodArtist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineBrushstroke.class, BloodArtist.class, GrizzlyBears.class, SculptingSteel.class})
class SanguineBrushstrokeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Blood token and conjures Blood Artist onto the battlefield")
    void entersWithBloodAndBloodArtist() {
        harness.setHand(player1, List.of(new SanguineBrushstroke()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Blood Artist")).hasSize(1);
        assertThat(findPermanent(player1, "Blood Artist").getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Blood token makes each opponent lose 1 life and you gain 1 life")
    void bloodSacrificeDrainsOpponents() {
        harness.setHand(player1, List.of(new SanguineBrushstroke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blood = findPermanent(player1, "Blood");
        int bloodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blood);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, bloodIndex, null, null);
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing a nontoken copy of Blood does not trigger the life drain")
    void nontokenBloodSacrificeDoesNotDrain() {
        harness.setHand(player1, List.of(new SanguineBrushstroke()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blood = findPermanent(player1, "Blood");
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());

        Permanent copiedBlood = findPermanents(player1, "Blood").stream()
                .filter(permanent -> !permanent.getId().equals(blood.getId()))
                .findFirst().orElseThrow();
        assertThat(copiedBlood.getCard().isToken()).isFalse();
        harness.setHand(player1, List.of(new SanguineBrushstroke()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(copiedBlood), null, null);
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Blood")).containsExactly(blood);
        harness.assertInGraveyard(player1, "Sculpting Steel");
    }

    @Test
    @DisplayName("An opponent's Blood sacrifice only triggers their own Brushstroke")
    void opponentBloodSacrificeDoesNotTriggerControllerBrushstroke() {
        harness.addToBattlefield(player1, new SanguineBrushstroke());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SanguineBrushstroke()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blood = findPermanent(player2, "Blood");
        harness.setHand(player2, List.of(new SanguineBrushstroke()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player2, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }
}
