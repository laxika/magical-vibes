package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxOfTheGuildpact.class, Shock.class, Mortify.class, ProdigalSorcerer.class,
        Humility.class, IcyManipulator.class})
class SphinxOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Sphinx of the Guildpact is all five colors")
    void isAllColors() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheGuildpact());

        assertThat(gqs.getEffectiveColors(gd, sphinx))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("Opponent's monocolored spells cannot target Sphinx of the Guildpact")
    void opponentMonocoloredSpellCannotTarget() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SphinxOfTheGuildpact());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("monocolored");
    }

    @Test
    @DisplayName("Multicolored spells can target Sphinx of the Guildpact")
    void multicoloredSpellCanTarget() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SphinxOfTheGuildpact());
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, sphinx.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getClass() == Mortify.class);
    }

    @Test
    @DisplayName("Opponent's monocolored abilities cannot target Sphinx of the Guildpact")
    void opponentMonocoloredAbilityCannotTarget() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SphinxOfTheGuildpact());
        addCreatureReady(player1, new ProdigalSorcerer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("monocolored");
    }

    @Test
    @DisplayName("Controller's monocolored spell can target the Sphinx")
    void controllerMonocoloredSpellCanTarget() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheGuildpact());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, sphinx.getId());
        harness.passBothPriorities();

        assertThat(sphinx.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Sphinx of the Guildpact");
    }

    @Test
    @DisplayName("Controller's monocolored ability can target the Sphinx")
    void controllerMonocoloredAbilityCanTarget() {
        addCreatureReady(player1, new ProdigalSorcerer());
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheGuildpact());

        harness.activateAbility(player1, 0, null, sphinx.getId());
        harness.passBothPriorities();

        assertThat(sphinx.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's colorless ability can target the Sphinx")
    void opponentColorlessAbilityCanTarget() {
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SphinxOfTheGuildpact());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, sphinx.getId());
        harness.passBothPriorities();

        assertThat(sphinx.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Losing all abilities removes hexproof from monocolored")
    void losingAllAbilitiesAllowsOpponentMonocoloredTargeting() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SphinxOfTheGuildpact());
        harness.castFromHand(player1, new Humility(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, sphinx.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sphinx of the Guildpact");
        harness.assertInGraveyard(player2, "Sphinx of the Guildpact");
    }
}
