package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SachiDaughterOfSeshiroTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent addSachi(Player player) {
        Permanent permanent = new Permanent(new SachiDaughterOfSeshiro());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Other Snakes you control get +0/+1")
    void boostsOtherOwnSnakes() {
        harness.addToBattlefield(player1, createCreature("Sakura Snake", 1, 1, CardSubtype.SNAKE));
        addSachi(player1);

        Permanent snake = findPermanent(player1, "Sakura Snake");
        var bonus = gqs.computeStaticBonus(gd, snake);

        assertThat(bonus.power()).isZero();
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sachi does not boost herself")
    void doesNotBoostSelf() {
        Permanent sachi = addSachi(player1);

        assertThat(gqs.computeStaticBonus(gd, sachi).toughness()).isZero();
    }

    @Test
    @DisplayName("Snakes an opponent controls are not boosted")
    void doesNotBoostOpponentSnakes() {
        harness.addToBattlefield(player2, createCreature("Sakura Snake", 1, 1, CardSubtype.SNAKE));
        addSachi(player1);

        Permanent snake = findPermanent(player2, "Sakura Snake");

        assertThat(gqs.computeStaticBonus(gd, snake).toughness()).isZero();
    }

    @Test
    @DisplayName("Non-Snake creatures you control are not boosted")
    void doesNotBoostNonSnakes() {
        harness.addToBattlefield(player1, createCreature("Grizzly Bears", 2, 2, CardSubtype.BEAR));
        addSachi(player1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.computeStaticBonus(gd, bears).toughness()).isZero();
    }

    @Test
    @DisplayName("Shamans you control gain the {T}: Add {G}{G} ability")
    void grantsManaAbilityToShamans() {
        harness.addToBattlefield(player1, createCreature("Orochi Leafcaller", 1, 1, CardSubtype.SHAMAN));
        addSachi(player1);

        Permanent shaman = findPermanent(player1, "Orochi Leafcaller");
        var granted = gqs.computeStaticBonus(gd, shaman).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().getDescription()).isEqualTo("{T}: Add {G}{G}.");
    }

    @Test
    @DisplayName("Sachi is a Shaman, so she gains the mana ability herself")
    void grantsManaAbilityToSelf() {
        Permanent sachi = addSachi(player1);

        assertThat(gqs.computeStaticBonus(gd, sachi).grantedActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Non-Shaman creatures you control gain nothing")
    void doesNotGrantToNonShamans() {
        harness.addToBattlefield(player1, createCreature("Sakura Snake", 1, 1, CardSubtype.SNAKE));
        addSachi(player1);

        Permanent snake = findPermanent(player1, "Sakura Snake");

        assertThat(gqs.computeStaticBonus(gd, snake).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Activating the granted ability taps the Shaman and adds {G}{G}")
    void grantedAbilityProducesTwoGreenMana() {
        harness.addToBattlefield(player1, createCreature("Orochi Leafcaller", 1, 1, CardSubtype.SHAMAN));
        addSachi(player1);
        Permanent shaman = findPermanent(player1, "Orochi Leafcaller");
        shaman.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
