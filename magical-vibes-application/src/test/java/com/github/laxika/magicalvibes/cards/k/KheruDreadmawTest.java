package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KheruDreadmawTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndGainsItsToughness() {
        addDreadmawReady(player1);
        addCreatureReady(player1, createCreature("Test Creature", 2, 5));
        harness.setLife(player1, 10);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Test Creature");
    }

    @Test
    void cannotSacrificeKheruDreadmawItself() {
        addDreadmawReady(player1);
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addDreadmawReady(Player player) {
        Permanent permanent = new Permanent(new KheruDreadmaw());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
