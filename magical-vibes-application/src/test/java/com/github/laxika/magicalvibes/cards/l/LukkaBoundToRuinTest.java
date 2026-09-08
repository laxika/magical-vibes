package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LukkaBoundToRuinTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds red and green mana restricted to creatures")
    void plusOneAddsRestrictedCreatureMana() {
        addReadyLukka(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("-1 creates a toxic 3/3 Phyrexian Beast")
    void minusOneCreatesToxicBeast() {
        addReadyLukka(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent beast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Phyrexian Beast"))
                .findFirst()
                .orElseThrow();
        assertThat(beast.getCard().getPower()).isEqualTo(3);
        assertThat(beast.getCard().getToughness()).isEqualTo(3);
        assertThat(beast.getCard().getKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.TOXIC);
        assertThat(beast.getCard().getSubtypes()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardSubtype.PHYREXIAN,
                com.github.laxika.magicalvibes.model.CardSubtype.BEAST);
    }

    @Test
    @DisplayName("-4 uses greatest creature power at activation and can damage a planeswalker")
    void minusFourUsesGreatestPowerAndDamagesPlaneswalker() {
        Permanent lukka = addReadyLukka(player1, 5);
        GrizzlyBears largeBearCard = new GrizzlyBears();
        largeBearCard.setPower(4);
        largeBearCard.setToughness(4);
        Permanent largeBear = harness.addToBattlefieldAndReturn(player1, largeBearCard);
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ajani = new Permanent(new AjaniOutlandChaperone());
        ajani.setCounterCount(CounterType.LOYALTY, 5);
        ajani.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ajani);

        harness.activateAbilityWithDamageAssignments(player1, 0, 2, null,
                Map.of(targetBear.getId(), 2, ajani.getId(), 2));
        gd.playerBattlefields.get(player1.getId()).remove(largeBear);
        harness.passBothPriorities();

        assertThat(lukka.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-4 cannot target a player")
    void minusFourCannotTargetPlayer() {
        addReadyLukka(player1, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 2, null, Map.of(player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyLukka(Player player, int loyalty) {
        Permanent permanent = new Permanent(new LukkaBoundToRuin());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
