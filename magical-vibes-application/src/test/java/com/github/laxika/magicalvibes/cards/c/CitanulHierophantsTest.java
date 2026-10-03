package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CitanulHierophants.class, GorillaWarrior.class, GloriousAnthem.class})
class CitanulHierophantsTest extends BaseCardTest {

    @Test
    @DisplayName("Citanul Hierophants and other creatures you control can tap for green mana")
    void grantsGreenManaAbilityToControlledCreaturesIncludingItself() {
        addCreatureReady(player1, new CitanulHierophants());
        addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Citanul Hierophants does not grant the ability to creatures an opponent controls")
    void doesNotGrantAbilityToOpponentCreatures() {
        harness.addToBattlefield(player1, new CitanulHierophants());
        addCreatureReady(player2, new GorillaWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Citanul Hierophants does not grant the ability to noncreatures you control")
    void doesNotGrantAbilityToControlledNoncreatures() {
        harness.addToBattlefield(player1, new CitanulHierophants());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted mana abilities tap their creatures and resolve without using the stack")
    void grantedManaAbilityPaysTapCostAndResolvesImmediately() {
        addCreatureReady(player1, new CitanulHierophants());
        var warrior = addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(warrior.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents using the granted tap ability")
    void summoningSickCreaturesCannotActivateGrantedManaAbility() {
        harness.addToBattlefield(player1, new CitanulHierophants());
        harness.addToBattlefield(player1, new GorillaWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A summoning sick Hierophants still grants its ability to ready creatures")
    void summoningSicknessOfSourceDoesNotPreventGrant() {
        harness.addToBattlefield(player1, new CitanulHierophants());
        addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The grant applies to later creatures even while Hierophants is tapped")
    void grantsAbilityToLaterCreaturesWhileTapped() {
        addCreatureReady(player1, new CitanulHierophants());
        harness.activateAbility(player1, 0, 0, null, null);
        addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures lose the granted ability when Hierophants leaves the battlefield")
    void grantEndsWhenSourceLeavesBattlefield() {
        var hierophants = addCreatureReady(player1, new CitanulHierophants());
        addCreatureReady(player1, new GorillaWarrior());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.performUntapStep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(hierophants);
        gd.playerGraveyards.get(player1.getId()).add(hierophants.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
