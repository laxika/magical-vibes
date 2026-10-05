package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SunderingVitae;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollenbrightWings.class, Watchwolf.class, Forest.class, SunderingVitae.class})
class PollenbrightWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pollenbright Wings on a creature grants flying")
    void castingOnCreatureGrantsFlying() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        harness.setHand(player1, List.of(new PollenbrightWings()));
        addPollenbrightWingsMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PollenbrightWings
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Pollenbright Wings cannot enchant a land")
    void cannotEnchantALand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PollenbrightWings()));
        addPollenbrightWingsMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Pollenbright Wings only gives flying to the enchanted creature")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        Permanent otherCreature = addCreatureReady(player1, new Watchwolf());
        attachWings(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Combat damage creates that many Saproling tokens")
    void combatDamageCreatesSaprolings() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachWings(creature);
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Saproling"))
                .hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Only the enchanted creature's combat damage creates Saprolings")
    void onlyEnchantedCreatureTriggers() {
        Permanent enchantedCreature = addCreatureReady(player1, new Watchwolf());
        Permanent otherCreature = addCreatureReady(player1, new Watchwolf());
        attachWings(enchantedCreature);
        enchantedCreature.setAttacking(true);
        otherCreature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
    }

    @Test
    @DisplayName("Combat damage to a creature does not trigger Pollenbright Wings")
    void combatDamageToCreatureDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new Watchwolf());
        attachWings(attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Watchwolf());
        attachWings(player2, blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("The Aura controller creates tokens when an opponent's creature deals combat damage")
    void auraControllerCreatesTokensForOpponentCreatureDamage() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        attachWings(player1, creature);
        creature.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Zero combat damage does not trigger token creation")
    void zeroCombatDamageDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        creature.setPowerModifier(-3);
        attachWings(creature);
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Token count uses damage dealt even if the creature's power changes")
    void tokenCountUsesDamageDealt() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachWings(creature);
        creature.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        creature.setPowerModifier(4);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop its token trigger")
    void tokenTriggerSurvivesAuraDestruction() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachWings(creature);
        Permanent aura = findPermanent(player1, "Pollenbright Wings");
        creature.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        harness.setHand(player1, List.of(new SunderingVitae()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Pollenbright Wings")).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
    }

    private void addPollenbrightWingsMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void attachWings(Permanent creature) {
        attachWings(player1, creature);
    }

    private void attachWings(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new PollenbrightWings());
        aura.setAttachedTo(creature.getId());
    }
}
