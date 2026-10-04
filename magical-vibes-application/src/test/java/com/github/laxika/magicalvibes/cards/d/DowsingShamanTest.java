package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.f.FistsOfIronwood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DowsingShaman.class, FistsOfIronwood.class, BorosSwiftblade.class})
class DowsingShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target enchantment card from the graveyard to its controller's hand")
    void returnsTargetEnchantmentToHand() {
        Permanent shaman = addCreatureReady(player1, new DowsingShaman());
        Card enchantment = new FistsOfIronwood();
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();

        harness.activateAbility(player1, 0, null, enchantment.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(enchantment);
        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-enchantment card in the graveyard")
    void cannotTargetNonEnchantmentCard() {
        Permanent shaman = addCreatureReady(player1, new DowsingShaman());
        Card creature = new BorosSwiftblade();
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Cannot target an enchantment card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Permanent shaman = addCreatureReady(player1, new DowsingShaman());
        Card enchantment = new FistsOfIronwood();
        harness.setGraveyard(player2, List.of(enchantment));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, enchantment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enchantment);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DowsingShaman());
        Card enchantment = new FistsOfIronwood();
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, enchantment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        Permanent shaman = addCreatureReady(player1, new DowsingShaman());
        shaman.tap();
        Card enchantment = new FistsOfIronwood();
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, enchantment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment);
    }

    @Test
    @DisplayName("Cannot pay the activation cost without green mana")
    void requiresGreenMana() {
        Permanent shaman = addCreatureReady(player1, new DowsingShaman());
        Card enchantment = new FistsOfIronwood();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, enchantment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not return a different enchantment when the target leaves the graveyard")
    void doesNotSubstituteAnotherEnchantmentForMissingTarget() {
        Permanent shaman = addCreatureReady(player1, new DowsingShaman());
        Card target = new FistsOfIronwood();
        Card other = new FistsOfIronwood();
        harness.setGraveyard(player1, List.of(target, other));
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        harness.setGraveyard(player1, List.of(other));
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after the Shaman leaves the battlefield")
    void resolvesWithoutSourceOnBattlefield() {
        DowsingShaman source = new DowsingShaman();
        addCreatureReady(player1, source);
        Card enchantment = new FistsOfIronwood();
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();
        harness.activateAbility(player1, 0, null, enchantment.getId(), Zone.GRAVEYARD);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(enchantment, source));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
