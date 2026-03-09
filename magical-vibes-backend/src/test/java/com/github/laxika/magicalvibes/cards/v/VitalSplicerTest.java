package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VitalSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.setHand(player1, List.of(new VitalSplicer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Vital Splicer + Golem token

        Permanent golemToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();
        assertThat(golemToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golemToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golemToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(golemToken.getEffectivePower()).isEqualTo(3);
        assertThat(golemToken.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Regenerate ability targets a Golem and puts ability on stack")
    void activatingRegenTargetsGolem() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent golemToken = addGolemToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, golemToken.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(golemToken.getId());
    }

    @Test
    @DisplayName("Resolving regenerate grants a regeneration shield to target Golem")
    void resolvingRegenGrantsShield() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent golemToken = addGolemToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, golemToken.getId());
        harness.passBothPriorities();

        assertThat(golemToken.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate regenerate multiple times to stack shields")
    void canStackMultipleRegenerationShields() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent golemToken = addGolemToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, golemToken.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, golemToken.getId());
        harness.passBothPriorities();

        assertThat(golemToken.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-Golem creature with regenerate ability")
    void cannotTargetNonGolem() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target opponent's Golem with regenerate ability")
    void cannotTargetOpponentGolem() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent opponentGolem = addGolemToken(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentGolem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate regenerate without enough mana")
    void cannotActivateRegenWithoutMana() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent golemToken = addGolemToken(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, golemToken.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration fizzles if target Golem is removed before resolution")
    void regenFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent golemToken = addGolemToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, golemToken.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(golemToken);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Regenerate ability does not require tapping Vital Splicer")
    void regenDoesNotRequireTap() {
        harness.addToBattlefield(player1, new VitalSplicer());
        Permanent golemToken = addGolemToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Activate twice without tapping — both should succeed
        harness.activateAbility(player1, 0, null, golemToken.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, golemToken.getId());

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addGolemToken(Player player) {
        Card golemCard = new Card();
        golemCard.setName("Phyrexian Golem");
        golemCard.setType(CardType.CREATURE);
        golemCard.setAdditionalTypes(java.util.Set.of(CardType.ARTIFACT));
        golemCard.setSubtypes(List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM));
        golemCard.setPower(3);
        golemCard.setToughness(3);
        golemCard.setToken(true);

        Permanent permanent = new Permanent(golemCard);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
