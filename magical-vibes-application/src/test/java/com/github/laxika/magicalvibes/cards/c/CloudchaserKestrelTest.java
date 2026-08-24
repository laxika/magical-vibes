package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudchaserKestrel.class, AngelicChorus.class, GrizzlyBears.class})
class CloudchaserKestrelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and destroys target enchantment")
    void entersAndDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new CloudchaserKestrel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, 0, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudchaser Kestrel");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature with the enter-the-battlefield ability")
    void cannotTargetCreatureWithEnterTheBattlefieldAbility() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CloudchaserKestrel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Target permanent becomes white until end of turn")
    void targetPermanentBecomesWhiteUntilEndOfTurn() {
        harness.addToBattlefield(player1, new CloudchaserKestrel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }
}
