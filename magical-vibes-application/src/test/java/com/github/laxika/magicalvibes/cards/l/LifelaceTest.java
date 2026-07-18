package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LifelaceTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes green, replacing its previous colors (CR 105.3)")
    void permanentBecomesGreen() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Fugitive Wizard"));

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The color change has no duration — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Fugitive Wizard"));

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);

        // End-of-turn cleanup expires until-end-of-turn floating effects; Lifelace's is permanent.
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Targeting a creature spell makes the permanent it becomes green (CR 613.7)")
    void spellTargetCarriesColorToPermanent() {
        harness.setHand(player1, List.of(new Lifelace(), new FugitiveWizard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Fugitive Wizard creature spell goes on the stack (index 1; Lifelace stays at index 0).
        harness.castCreature(player1, 1);
        UUID wizardSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, wizardSpellId);
        harness.passBothPriorities(); // resolve Lifelace on the spell
        harness.passBothPriorities(); // resolve the Fugitive Wizard spell

        Permanent wizard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fugitive Wizard"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, wizard)).containsExactly(CardColor.GREEN);
    }
}
