package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeralfVisionaryStitcher.class, AzureDrake.class})
class GeralfVisionaryStitcherTest extends BaseCardTest {

    @Test
    void sacrificesAnotherNontokenCreatureAndCreatesFlyingZombie() {
        Permanent geralf = addCreatureReady(player1, new GeralfVisionaryStitcher());
        Permanent drake = addCreatureReady(player1, new AzureDrake());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Azure Drake");
        assertThat(geralf.isTapped()).isTrue();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, geralf, Keyword.FLYING)).isFalse();
        assertThat(drake).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void cannotSacrificeSourceOrToken() {
        addCreatureReady(player1, new GeralfVisionaryStitcher());
        harness.addToBattlefield(player1, createTokenCreature());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Token Creature");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }
}
