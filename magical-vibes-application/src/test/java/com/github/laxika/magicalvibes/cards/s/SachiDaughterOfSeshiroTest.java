package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OrochiSustainer;
import com.github.laxika.magicalvibes.cards.r.Rootrunner;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SachiDaughterOfSeshiro.class, OrochiSustainer.class, Rootrunner.class})
class SachiDaughterOfSeshiroTest extends BaseCardTest {

    private Permanent addSachi(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SachiDaughterOfSeshiro());
    }

    private Permanent addNonCreatureShamanPermanent(Player player) {
        Rootrunner shaman = new Rootrunner();
        shaman.setType(CardType.ENCHANTMENT);
        shaman.setAdditionalTypes(Set.of(CardType.KINDRED));
        shaman.setSubtypes(List.of(CardSubtype.SHAMAN));
        return harness.addToBattlefieldAndReturn(player, shaman);
    }

    @Test
    @DisplayName("Other Snakes you control get +0/+1")
    void boostsOtherOwnSnakes() {
        harness.addToBattlefield(player1, new OrochiSustainer());
        addSachi(player1);

        Permanent snake = findPermanent(player1, "Orochi Sustainer");
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
        harness.addToBattlefield(player2, new OrochiSustainer());
        addSachi(player1);

        Permanent snake = findPermanent(player2, "Orochi Sustainer");

        assertThat(gqs.computeStaticBonus(gd, snake).toughness()).isZero();
    }

    @Test
    @DisplayName("Non-Snake creatures you control are not boosted")
    void doesNotBoostNonSnakes() {
        harness.addToBattlefield(player1, new Rootrunner());
        addSachi(player1);

        Permanent bears = findPermanent(player1, "Rootrunner");

        assertThat(gqs.computeStaticBonus(gd, bears).toughness()).isZero();
    }

    @Test
    @DisplayName("Shamans you control gain the {T}: Add {G}{G} ability")
    void grantsManaAbilityToShamans() {
        harness.addToBattlefield(player1, new OrochiSustainer());
        addSachi(player1);

        Permanent shaman = findPermanent(player1, "Orochi Sustainer");
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
        harness.addToBattlefield(player1, new Rootrunner());
        addSachi(player1);

        Permanent snake = findPermanent(player1, "Rootrunner");

        assertThat(gqs.computeStaticBonus(gd, snake).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Opponent Shamans do not gain the mana ability")
    void doesNotGrantToOpponentShamans() {
        harness.addToBattlefield(player2, new OrochiSustainer());
        addSachi(player1);

        Permanent shaman = findPermanent(player2, "Orochi Sustainer");

        assertThat(gqs.computeStaticBonus(gd, shaman).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Noncreature Shaman permanents you control gain the mana ability")
    void grantsManaAbilityToNonCreatureShamanPermanents() {
        Permanent shaman = addNonCreatureShamanPermanent(player1);
        addSachi(player1);

        assertThat(gqs.computeStaticBonus(gd, shaman).grantedActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Activating the granted ability taps the Shaman and adds {G}{G}")
    void grantedAbilityProducesTwoGreenMana() {
        Permanent shaman = addCreatureReady(player1, new OrochiSustainer());
        addSachi(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
