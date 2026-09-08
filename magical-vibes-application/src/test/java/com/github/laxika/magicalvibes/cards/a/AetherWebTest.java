package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherWeb.class, FountainOfYouth.class, GrizzlyBears.class})
class AetherWebTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1, reach, and can block creatures with shadow")
    void enchantedCreatureGetsBoostReachAndShadowBlocking() {
        Permanent blocker = addCreature(player2, false);
        enchant(blocker, player2);
        Permanent attacker = addCreature(player1, true);
        attacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.REACH)).isTrue();

        beginBlocking();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature loses Aether Web's effects when the Aura leaves")
    void effectsLostWhenAuraLeaves() {
        Permanent creature = addCreature(player1, false);
        Permanent aura = enchant(creature, player1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AetherWeb()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(Player owner, boolean shadow) {
        Card card = new GrizzlyBears();
        if (shadow) {
            card.setKeywords(Set.of(Keyword.SHADOW));
        }
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(creature);
        return creature;
    }

    private Permanent enchant(Permanent creature, Player owner) {
        Permanent aura = new Permanent(new AetherWeb());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
        return aura;
    }

    private void beginBlocking() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
