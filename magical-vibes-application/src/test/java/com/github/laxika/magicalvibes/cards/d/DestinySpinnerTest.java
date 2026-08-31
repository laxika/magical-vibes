package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.e.EtherealArmor;
import com.github.laxika.magicalvibes.cards.f.FamiliarGround;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestinySpinner.class, Cancel.class, EtherealArmor.class, FamiliarGround.class,
        Forest.class, GrizzlyBears.class, Shock.class})
class DestinySpinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Destiny Spinner protects creature spells you control")
    void protectsCreatureSpells() {
        addSpinner(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Destiny Spinner protects enchantment spells you control")
    void protectsEnchantmentSpells() {
        addSpinner(player1);
        Permanent bears = addCreature(player1);
        EtherealArmor armor = new EtherealArmor();
        harness.setHand(player1, List.of(armor));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, armor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ethereal Armor");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Destiny Spinner does not protect instant spells you control")
    void doesNotProtectInstantSpells() {
        addSpinner(player1);
        Permanent bears = addCreature(player1);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("The animated land uses the enchantment count at resolution and stays a land")
    void animatesLandUsingEnchantmentCount() {
        addSpinner(player1);
        Permanent land = addLand(player1);
        Permanent otherEnchantment = new Permanent(new FamiliarGround());
        gd.playerBattlefields.get(player1.getId()).add(otherEnchantment);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(land.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(otherEnchantment);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
    }

    @Test
    @DisplayName("Destiny Spinner cannot target an opponent's land")
    void cannotTargetOpponentsLand() {
        addSpinner(player1);
        Permanent opponentLand = addLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSpinner(com.github.laxika.magicalvibes.model.Player player) {
        Permanent spinner = new Permanent(new DestinySpinner());
        spinner.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(spinner);
        return spinner;
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addLand(com.github.laxika.magicalvibes.model.Player player) {
        Permanent land = new Permanent(new Forest());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
