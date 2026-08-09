package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmuletOfSafekeepingTest extends BaseCardTest {

    @Test
    @DisplayName("Creature tokens get -1/-0, but nontoken creatures do not")
    void debuffsCreatureTokens() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.addToBattlefield(player1, createCreature("Soldier Token", 2, 2, true));
        harness.addToBattlefield(player2, createCreature("Zombie Token", 3, 3, true));
        harness.addToBattlefield(player1, createCreature("Grizzly Bears", 2, 2, false));

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Soldier Token"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Soldier Token"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Zombie Token"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters an opponent's spell that targets its controller")
    void countersOpponentSpellTargetingController() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not counter its controller's own spell")
    void doesNotCounterControllersOwnSpell() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Counters an opponent's ability that targets its controller")
    void countersOpponentAbilityTargetingController() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(spellcaster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent may pay {1} to keep the spell")
    void opponentMayPay() {
        harness.addToBattlefield(player1, new AmuletOfSafekeeping());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Shock");
    }

    private Card createCreature(String name, int power, int toughness, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(token);
        return card;
    }
}
