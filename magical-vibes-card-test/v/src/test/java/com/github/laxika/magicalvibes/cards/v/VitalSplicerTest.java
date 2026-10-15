package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VitalSplicer.class, GrizzlyBears.class, BeastWithin.class})
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

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");
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
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(golemToken.getId());
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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
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

    @Test
    @DisplayName("Regeneration prevents destruction and consumes the shield")
    void regenerationPreventsDestruction() {
        harness.enterBattlefieldAndReturn(player1, new VitalSplicer());
        resolveAllTriggers();
        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, golem.getId());
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isFalse();
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, golem.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(golem);
        assertThat(golem.isTapped()).isTrue();
        assertThat(golem.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration resolves after a tapped Vital Splicer leaves the battlefield")
    void regenerationResolvesWithoutSource() {
        harness.enterBattlefieldAndReturn(player1, new VitalSplicer());
        resolveAllTriggers();
        Permanent splicer = findPermanent(player1, "Vital Splicer");
        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        splicer.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, golem.getId());

        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, splicer.getId());
        harness.assertNotOnBattlefield(player1, "Vital Splicer");
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(1);
        assertThat(golem.isTapped()).isFalse();
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

        Permanent permanent = harness.addToBattlefieldAndReturn(player, golemCard);
        permanent.setSummoningSick(false);

        return permanent;
    }
}
