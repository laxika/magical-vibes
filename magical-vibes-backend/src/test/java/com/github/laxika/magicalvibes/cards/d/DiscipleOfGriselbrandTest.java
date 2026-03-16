package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToXValueEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscipleOfGriselbrandTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with {1} mana cost, sacrifice creature cost, and gain life effect")
    void hasCorrectAbilityStructure() {
        DiscipleOfGriselbrand card = new DiscipleOfGriselbrand();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{1}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GainLifeEqualToXValueEffect.class);

        SacrificeCreatureCost sacCost = (SacrificeCreatureCost) ability.getEffects().get(0);
        assertThat(sacCost.trackSacrificedToughness()).isTrue();
        assertThat(sacCost.trackSacrificedPower()).isFalse();
        assertThat(sacCost.trackSacrificedManaValue()).isFalse();
    }

    // ===== Sacrifice and gain life =====

    @Test
    @DisplayName("Sacrificing a 2/2 creature gains 2 life")
    void sacrificing2_2CreatureGains2Life() {
        Permanent disciple = addDiscipleReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);

        // Grizzly Bears should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sacrificing a creature with higher toughness gains more life")
    void sacrificingHighToughnessCreatureGainsMoreLife() {
        Permanent disciple = addDiscipleReady(player1);
        Permanent beefy = addCreatureReady(player1, createCreature("Beefy Beast", 1, 5));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, beefy.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Sacrificing a 1/1 creature gains 1 life")
    void sacrificing1_1CreatureGains1Life() {
        Permanent disciple = addDiscipleReady(player1);
        Permanent token = addCreatureReady(player1, createCreature("Goblin Token", 1, 1));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Can sacrifice Disciple of Griselbrand to its own ability")
    void canSacrificeItself() {
        Permanent disciple = addDiscipleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, disciple.getId());
        harness.passBothPriorities();

        // Disciple is 1/1, so gains 1 life
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Disciple of Griselbrand"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Disciple of Griselbrand"));
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        Permanent disciple = addDiscipleReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent token = addCreatureReady(player1, createCreature("Goblin Token", 1, 1));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.getLife(player1.getId());

        // Sacrifice Grizzly Bears (toughness 2)
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        // Sacrifice Goblin Token (toughness 1)
        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        // Total life gained: 2 + 1 = 3
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Requires {1} mana to activate")
    void requiresManaToActivate() {
        Permanent disciple = addDiscipleReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutSacrificeTarget() {
        Permanent disciple = addDiscipleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentCreature() {
        Permanent disciple = addDiscipleReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature you control");
    }

    // ===== Helper methods =====

    private Permanent addDiscipleReady(Player player) {
        DiscipleOfGriselbrand card = new DiscipleOfGriselbrand();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
