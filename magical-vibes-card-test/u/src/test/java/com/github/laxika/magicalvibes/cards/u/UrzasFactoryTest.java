package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(UrzasFactory.class)
class UrzasFactoryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Urza's Factory produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent factory = addCreatureReady(player1, new UrzasFactory());

        gs.tapPermanent(gd, player1, battlefieldIndex(factory));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(factory.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {7} and tapping Urza's Factory creates an Assembly-Worker token")
    void createsAssemblyWorkerToken() {
        Permanent factory = addCreatureReady(player1, new UrzasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, battlefieldIndex(factory), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Assembly-Worker");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ASSEMBLY_WORKER);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(factory.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Token creation pays seven mana and uses the stack without producing mana")
    void tokenCreationPaysManaAndUsesStack() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new UrzasFactory());
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.activateAbility(player1, battlefieldIndex(factory), 0, null, null);

        assertThat(factory.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Assembly-Worker")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Assembly-Worker")).isEqualTo(1);
        assertThat(countPermanents(player2, "Assembly-Worker")).isZero();
        assertThat(findPermanent(player1, "Assembly-Worker").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Six mana cannot pay the token ability's cost")
    void insufficientManaDoesNotPayCosts() {
        Permanent factory = addCreatureReady(player1, new UrzasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(factory), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(factory.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Assembly-Worker")).isZero();
    }

    @Test
    @DisplayName("A Factory tapped for mana cannot also pay the token ability's tap cost")
    void tappedFactoryCannotCreateToken() {
        Permanent factory = addCreatureReady(player1, new UrzasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        gs.tapPermanent(gd, player1, battlefieldIndex(factory));

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(factory), 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Assembly-Worker")).isZero();
    }

    @Test
    @DisplayName("The token ability resolves after Urza's Factory leaves the battlefield")
    void tokenAbilityResolvesWithoutSource() {
        Permanent factory = addCreatureReady(player1, new UrzasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, battlefieldIndex(factory), 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(factory);
        gd.playerGraveyards.get(player1.getId()).add(factory.getCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Assembly-Worker")).isEqualTo(1);
        assertThat(countPermanents(player2, "Assembly-Worker")).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
