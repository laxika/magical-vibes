package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivixAerieOfTheFiremind.class, Shock.class, Pyroclasm.class, GrizzlyBears.class})
class NivixAerieOfTheFiremindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent nivix = addReadyNivix();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(nivix.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles and lets its controller cast a top instant until the next turn")
    void castsTopInstantFromExile() {
        Card shock = activateExileAbility(new Shock());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("Lets its controller cast a top sorcery from exile")
    void castsTopSorceryFromExile() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card pyroclasm = activateExileAbility(new Pyroclasm());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, pyroclasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pyroclasm);
    }

    @Test
    @DisplayName("Exiles but does not allow casting a non-instant or non-sorcery")
    void doesNotAllowCastingCreatureFromExile() {
        Card bears = activateExileAbility(new GrizzlyBears());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyNivix() {
        Permanent nivix = harness.addToBattlefieldAndReturn(player1, new NivixAerieOfTheFiremind());
        nivix.setSummoningSick(false);
        return nivix;
    }

    private Card activateExileAbility(Card topCard) {
        Permanent nivix = addReadyNivix();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int nivixIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nivix);
        harness.activateAbility(player1, nivixIndex, 1, null, null);
        harness.passBothPriorities();
        return topCard;
    }
}
