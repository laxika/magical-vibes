package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.p.ProwlersHelm;
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

@CardUsed({MessengersSpeed.class, BronzeSable.class, ProwlersHelm.class})
class MessengersSpeedTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has haste and trample")
    void enchantedCreatureHasHasteAndTrample() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MessengersSpeed());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Granted keywords are lost when Messenger's Speed leaves the battlefield")
    void grantedKeywordsAreLostWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MessengersSpeed());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Messenger's Speed can enchant only a creature")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ProwlersHelm());
        harness.setHand(player1, List.of(new MessengersSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() ->
                        harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Messenger's Speed can enchant an opponent's creature without granting keywords to other creatures")
    void canEnchantOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new MessengersSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Messenger's Speed").getAttachedTo()).isEqualTo(opponentCreature.getId());
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can attack despite summoning sickness")
    void enchantedCreatureCanAttackImmediately() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new MessengersSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Messenger's Speed goes to the graveyard if its target leaves before resolution")
    void doesNotEnterWhenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.setHand(player1, List.of(new MessengersSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Messenger's Speed")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MessengersSpeed);
        assertThat(gd.stack).isEmpty();
    }
}
