package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GroveOfTheGuardian.class, DrudgeBeetle.class})
class GroveOfTheGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new GroveOfTheGuardian());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices itself and taps two creatures to create a vigilant Elemental")
    void createsElementalAfterPayingCosts() {
        harness.addToBattlefield(player1, new GroveOfTheGuardian());
        Permanent firstCreature = addCreatureReady(player1, new DrudgeBeetle());
        Permanent secondCreature = addCreatureReady(player1, new DrudgeBeetle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grove of the Guardian");
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getEffectivePower()).isEqualTo(8);
        assertThat(token.getEffectiveToughness()).isEqualTo(8);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Cannot activate the token ability without two untapped creatures")
    void requiresTwoUntappedCreatures() {
        harness.addToBattlefield(player1, new GroveOfTheGuardian());
        addCreatureReady(player1, new DrudgeBeetle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the tap cost, and the token waits for resolution")
    void summoningSickCreaturesCanPayCosts() {
        harness.addToBattlefield(player1, new GroveOfTheGuardian());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grove of the Guardian");
        harness.assertNotOnBattlefield(player1, "Grove of the Guardian");
        harness.assertNotOnBattlefield(player1, "Elemental");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(findPermanent(player1, "Elemental").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Elemental");
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay the cost")
    void rejectsTappedAndOpposingCreatures() {
        harness.addToBattlefield(player1, new GroveOfTheGuardian());
        addCreatureReady(player1, new DrudgeBeetle());
        Permanent tapped = addCreatureReady(player1, new DrudgeBeetle());
        tapped.tap();
        addCreatureReady(player2, new DrudgeBeetle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grove of the Guardian");
        harness.assertNotOnBattlefield(player1, "Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Grove cannot activate its token ability")
    void tappedGroveCannotActivate() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new GroveOfTheGuardian());
        grove.tap();
        addCreatureReady(player1, new DrudgeBeetle());
        addCreatureReady(player1, new DrudgeBeetle());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grove of the Guardian");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The token ability requires both green and white mana")
    void requiresBothColoredMana() {
        harness.addToBattlefield(player1, new GroveOfTheGuardian());
        addCreatureReady(player1, new DrudgeBeetle());
        addCreatureReady(player1, new DrudgeBeetle());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grove of the Guardian");
        assertThat(gd.stack).isEmpty();
    }
}
