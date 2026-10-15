package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GuardiansMagemark;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.i.IzzetGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FencersMagemark.class, GuardiansMagemark.class, GruulSignet.class, IzzetGuildmage.class})
class FencersMagemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches to a creature when cast")
    void attachesToCreatureWhenCast() {
        Permanent creature = addCreatureReady(player1, new IzzetGuildmage());
        harness.setHand(player1, List.of(new FencersMagemark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FencersMagemark
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Boosts and grants first strike to each enchanted creature you control")
    void boostsAndGrantsFirstStrikeToEnchantedCreaturesYouControl() {
        Permanent firstCreature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent secondCreature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent unenchantedCreature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent opponentCreature = addCreatureReady(player2, new IzzetGuildmage());
        attach(new FencersMagemark(), firstCreature, player1);
        attach(new GuardiansMagemark(), secondCreature, player2);

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, unenchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, unenchantedCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Stops affecting enchanted creatures when Fencer's Magemark leaves the battlefield")
    void stopsAffectingCreaturesWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent magemark = attach(new FencersMagemark(), creature, player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(magemark);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new GruulSignet());
        harness.setHand(player1, List.of(new FencersMagemark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new IzzetGuildmage());
        harness.setHand(player1, List.of(new FencersMagemark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fencer's Magemark");
        harness.assertNotOnBattlefield(player1, "Fencer's Magemark");
    }

    @Test
    @DisplayName("Can enchant an opponent's creature without granting it the bonus")
    void canEnchantOpponentCreatureWithoutBoostingIt() {
        Permanent ownCreature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent opponentCreature = addCreatureReady(player2, new IzzetGuildmage());
        attach(new GuardiansMagemark(), ownCreature, player2);
        harness.setHand(player1, List.of(new FencersMagemark()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FencersMagemark
                        && opponentCreature.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Multiple Magemarks stack their bonuses and track newly enchanted creatures")
    void multipleMagemarksStackAndTrackEnchantedCreatures() {
        Permanent firstCreature = addCreatureReady(player1, new IzzetGuildmage());
        Permanent secondCreature = addCreatureReady(player1, new IzzetGuildmage());
        attach(new FencersMagemark(), firstCreature, player1);
        attach(new FencersMagemark(), firstCreature, player1);

        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isFalse();

        Permanent otherAura = attach(new GuardiansMagemark(), secondCreature, player2);

        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player2.getId()).remove(otherAura);

        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FIRST_STRIKE)).isFalse();
    }
    private Permanent attach(Card auraCard, Permanent creature, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, auraCard);
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
