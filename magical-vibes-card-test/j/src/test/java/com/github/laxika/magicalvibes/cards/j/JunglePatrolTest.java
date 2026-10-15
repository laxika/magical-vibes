package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JunglePatrol.class})
class JunglePatrolTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 0/1 green Wall token with defender named Wood")
    void createsWoodToken() {
        addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wood = findPermanent(player1, "Wood");
        assertThat(wood.getCard().getPower()).isEqualTo(0);
        assertThat(wood.getCard().getToughness()).isEqualTo(1);
        assertThat(wood.getCard().getKeywords()).contains(Keyword.DEFENDER);
        assertThat(wood.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("First ability creates a green Wall creature token named Wood and taps Jungle Patrol")
    void createsCorrectWoodTokenCharacteristics() {
        Permanent patrol = addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wood = findPermanent(player1, "Wood");
        assertThat(patrol.isTapped()).isTrue();
        assertThat(wood.getCard().getName()).isEqualTo("Wood");
        assertThat(wood.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wood.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wood.getCard().getSubtypes()).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("Second ability sacrifices a Wood token to add {R}")
    void sacrificesWoodForRedMana() {
        addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wood");

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Wood");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability cannot be activated without a Wood token")
    void requiresWoodToken() {
        addCreatureReady(player1, new JunglePatrol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creating Wood requires green mana, not just two generic mana")
    void requiresGreenManaToCreateWood() {
        Permanent patrol = addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(patrol.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Wood");
    }

    @Test
    @DisplayName("Creating Wood pays one green and one generic mana and uses the stack")
    void paysManaAndCreatesWoodOnlyOnResolution() {
        Permanent patrol = addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(patrol.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertNotOnBattlefield(player1, "Wood");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wood")).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Jungle Patrol cannot create Wood")
    void summoningSicknessPreventsCreatingWood() {
        harness.addToBattlefield(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Wood");
    }

    @Test
    @DisplayName("A summoning-sick Jungle Patrol can sacrifice Wood made by another Patrol")
    void summoningSickPatrolCanSacrificeAnotherPatrolsWood() {
        addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new JunglePatrol());

        harness.activateAbility(player1, 2, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Wood");
        assertThat(patrol.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Wood token cannot pay Jungle Patrol's sacrifice cost")
    void cannotSacrificeOpponentsWood() {
        addCreatureReady(player1, new JunglePatrol());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent wood = findPermanent(player1, "Wood");
        gd.playerBattlefields.get(player1.getId()).remove(wood);
        gd.playerBattlefields.get(player2.getId()).add(wood);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Wood");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
