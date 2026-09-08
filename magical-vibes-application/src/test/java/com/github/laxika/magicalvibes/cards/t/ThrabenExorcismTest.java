package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LunarchVeteran;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrabenExorcism.class, AngelicChorus.class, GrizzlyBears.class, LunarchVeteran.class, WindSpirit.class})
class ThrabenExorcismTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a Spirit")
    void exilesSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WindSpirit());

        castExorcism(target);

        assertExiled(player2, "Wind Spirit");
    }

    @Test
    @DisplayName("Exiles a creature with disturb")
    void exilesCreatureWithDisturb() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LunarchVeteran());

        castExorcism(target);

        assertExiled(player2, "Lunarch Veteran");
    }

    @Test
    @DisplayName("Exiles an enchantment")
    void exilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        castExorcism(target);

        assertExiled(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Rejects a creature that is neither a Spirit nor has disturb")
    void rejectsOrdinaryCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThrabenExorcism()));
        addExorcismMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit, a creature with disturb, or an enchantment");
    }

    private void castExorcism(Permanent target) {
        harness.setHand(player1, List.of(new ThrabenExorcism()));
        addExorcismMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addExorcismMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void assertExiled(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        harness.assertNotOnBattlefield(player, cardName);
        harness.assertNotInGraveyard(player, cardName);
        assertThat(gd.getPlayerExiledCards(player.getId()))
                .anyMatch(card -> card.getName().equals(cardName));
    }
}
