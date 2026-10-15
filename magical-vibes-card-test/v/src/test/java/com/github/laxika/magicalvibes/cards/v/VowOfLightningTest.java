package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorderlandRanger;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
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

@CardUsed({VowOfLightning.class, BorderlandRanger.class, PrismaticLens.class, GarrukWildspeaker.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
class VowOfLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Vow of Lightning gives the enchanted creature +2/+2 and first strike")
    void grantsBoostAndFirstStrike() {
        Permanent creature = addCreatureReady(player1, new BorderlandRanger());
        attachVowOfLightning(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller or their planeswalkers")
    void enchantedCreatureCannotAttackAuraControllerOrPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new BorderlandRanger());
        attachVowOfLightning(player2, creature);
        Permanent planeswalker = addPlaneswalker(player2);

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction affects only the enchanted creature")
    void doesNotRestrictOtherCreatures() {
        Permanent enchanted = addCreatureReady(player1, new BorderlandRanger());
        addCreatureReady(player1, new BorderlandRanger());
        attachVowOfLightning(player2, enchanted);

        declareAttackers(player1, List.of(1));
    }

    @Test
    @DisplayName("The attack restriction ends when Vow of Lightning leaves the battlefield")
    void restrictionEndsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new BorderlandRanger());
        Permanent aura = attachVowOfLightning(player2, creature);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("Vow of Lightning can target only a creature")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());
        harness.setHand(player1, List.of(new VowOfLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Vow of Lightning resolves on an opposing creature and grants its benefits")
    void resolvesOnOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new BorderlandRanger());
        harness.setHand(player1, List.of(new VowOfLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Vow of Lightning").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(als.canAttackDefender(gd, creature, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("A creature enchanted by its own controller can attack the opponent")
    void canAttackOpponentWhenAuraIsControlledByAttacker() {
        Permanent creature = addCreatureReady(player1, new BorderlandRanger());
        attachVowOfLightning(player1, creature);

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("The attack restriction follows the current controller of the Aura")
    void restrictionFollowsAuraController() {
        Permanent creature = addCreatureReady(player1, new BorderlandRanger());
        Permanent aura = attachVowOfLightning(player2, creature);
        assertThat(als.canAttackDefender(gd, creature, player2.getId())).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(als.canAttackDefender(gd, creature, player2.getId())).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The boost and first strike end when the Aura leaves")
    void benefitsEndWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new BorderlandRanger());
        Permanent aura = attachVowOfLightning(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }
    @Test
    @DisplayName("Vow of Lightning does not prohibit attacking battles controlled by its controller")
    void canAttackBattleControlledByAuraController() {
        Permanent creature = addCreatureReady(player1, new BorderlandRanger());
        attachVowOfLightning(player1, creature);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());

        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).contains(battle.getId());
        assertThat(als.canAttackDefender(gd, creature, battle.getId())).isTrue();

        beginAttack(player1);
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
    }

    private Permanent attachVowOfLightning(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new VowOfLightning());
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
        return harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
    }
}
