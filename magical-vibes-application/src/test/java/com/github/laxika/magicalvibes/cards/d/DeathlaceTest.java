package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeathlaceTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes black, replacing its previous colors (CR 105.3)")
    void permanentBecomesBlack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change has no duration — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);

        // End-of-turn cleanup expires until-end-of-turn floating effects; Deathlace's is permanent.
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Targeting a creature spell makes the permanent it becomes black (CR 613.7)")
    void spellTargetCarriesColorToPermanent() {
        harness.setHand(player1, List.of(new Deathlace(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Grizzly Bears creature spell goes on the stack (index 1; Deathlace stays at index 0).
        harness.castCreature(player1, 1);
        UUID bearsSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, bearsSpellId);
        harness.passBothPriorities(); // resolve Deathlace on the spell
        harness.passBothPriorities(); // resolve the Grizzly Bears spell

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLACK);
    }
}
