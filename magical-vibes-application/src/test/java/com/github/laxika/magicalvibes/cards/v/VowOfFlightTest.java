package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VowOfFlight.class, GrizzlyBears.class, LotusPetal.class})
class VowOfFlightTest extends BaseCardTest {

    @Test
    @DisplayName("Vow of Flight gives the enchanted creature +2/+2 and flying")
    void grantsBoostAndFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachVowOfFlight(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller")
    void enchantedCreatureCannotAttackAuraController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachVowOfFlight(player2, creature);

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller's planeswalker")
    void enchantedCreatureCannotAttackAuraControllersPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachVowOfFlight(player2, creature);
        Permanent planeswalker = addPlaneswalker(player2);

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The attack restriction ends when Vow of Flight leaves the battlefield")
    void restrictionEndsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachVowOfFlight(player2, creature);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, player2.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LotusPetal());
        harness.setHand(player1, List.of(new VowOfFlight()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting Vow of Flight can enchant an opponent's creature")
    void resolvesOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VowOfFlight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vow of Flight").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An enchanted creature can attack the Aura controller's opponent")
    void ownEnchantedCreatureCanAttackOpponent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachVowOfFlight(player1, creature);

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("The boost and granted flying end when the Aura leaves")
    void bonusesEndWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachVowOfFlight(player1, creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @CardUsed({InvasionOfZendikar.class})
    @DisplayName("Vow of Flight does not prevent attacking a battle controlled by its controller")
    void canAttackBattleControlledByAuraController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachVowOfFlight(player1, creature);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        beginAttack(player1);

        assertThat(als.canAttackDefender(gd, creature, battle.getId())).isTrue();
    }

    private Permanent attachVowOfFlight(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new VowOfFlight());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
