package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonbroodsRelic.class, GrizzlyBears.class})
class DragonbroodsRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and an untapped creature to add mana of the chosen color")
    void tapsCreatureForMana() {
        Permanent relic = addReady(player1, new DragonbroodsRelic());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, relic), 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(relic.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates an all-colored Dragon whose enter trigger deals 3 damage")
    void createsReliquaryDragon() {
        Permanent relic = addReady(player1, new DragonbroodsRelic());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, relic), 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dragonbroods' Relic");
        Permanent dragon = findPermanent(player1, "Reliquary Dragon");
        assertThat(dragon.getEffectivePower()).isEqualTo(4);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getColors()).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.LIFELINK);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The Dragon-making ability cannot be activated outside sorcery speed")
    void dragonAbilityRequiresSorcerySpeed() {
        Permanent relic = addReady(player1, new DragonbroodsRelic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addRequiredMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, relic), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The mana ability requires an untapped creature to tap")
    void manaAbilityRequiresCreature() {
        Permanent relic = addReady(player1, new DragonbroodsRelic());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, relic), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addRequiredMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
