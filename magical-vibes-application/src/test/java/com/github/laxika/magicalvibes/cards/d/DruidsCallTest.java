package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DruidsCall.class, HillGiant.class, LightningBolt.class, RayOfCommand.class, Shock.class, Swamp.class})
class DruidsCallTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to the enchanted creature creates that many Squirrel tokens")
    void damageCreatesTokensEqualToDamage() {
        Permanent giant = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        resolveAllTriggers();

        List<Permanent> squirrels = squirrelTokens(player2);
        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.getCard().getPower()).isEqualTo(1);
            assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
        });
        assertThat(squirrelTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("The trigger still creates tokens when lethal damage removes the Aura and creature")
    void triggerResolvesAfterEnchantedCreatureDies() {
        Permanent giant = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        resolveAllTriggers();

        assertThat(squirrelTokens(player2)).hasSize(3);
        assertThat(squirrelTokens(player1)).isEmpty();
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Druid's Call");
    }

    @Test
    @DisplayName("Druid's Call cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = findPermanent(player1, "Swamp");
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The Aura's controller controls the triggered ability even on an opponent's creature")
    void auraControllerControlsDamageTrigger() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        assertThat(squirrelTokens(player2)).hasSize(2);
    }

    @Test
    @DisplayName("The creature's controller at resolution receives the tokens after a control change")
    void controlChangeBeforeResolutionChangesTokenRecipient() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.assertOnBattlefield(player1, "Hill Giant");
        resolveAllTriggers();

        assertThat(squirrelTokens(player1)).hasSize(2);
        assertThat(squirrelTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("Combat damage triggers token creation even when both creatures die")
    void lethalCombatDamageCreatesTokens() {
        addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(squirrelTokens(player2)).hasSize(3);
        assertThat(squirrelTokens(player1)).isEmpty();
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Druid's Call");
    }

    @Test
    @DisplayName("Each separate damage event creates tokens, including the final lethal event")
    void repeatedDamageTriggersEachTime() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, giant.getId());
        resolveAllTriggers();
        assertThat(squirrelTokens(player2)).hasSize(2);

        harness.castAndResolveInstant(player1, 0, giant.getId());
        resolveAllTriggers();
        assertThat(squirrelTokens(player2)).hasSize(4);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Damage to a different creature does not trigger Druid's Call")
    void damageToUnenchantedCreatureDoesNotCreateTokens() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, other.getId());
        resolveAllTriggers();

        assertThat(squirrelTokens(player1)).isEmpty();
        assertThat(squirrelTokens(player2)).isEmpty();
    }

    private List<Permanent> squirrelTokens(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanents(player, "Squirrel").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
