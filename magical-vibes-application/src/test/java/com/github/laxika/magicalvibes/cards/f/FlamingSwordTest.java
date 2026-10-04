package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MercadianAtlas;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamingSword.class, FreshVolunteers.class, MercadianAtlas.class})
class FlamingSwordTest extends BaseCardTest {

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreature();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FlamingSword()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FlamingSword
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void enchantedCreatureGetsPowerBoostAndFirstStrike() {
        Permanent creature = addCreature();
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void effectsEndWhenAuraLeavesBattlefield() {
        Permanent creature = addCreature();
        Permanent aura = attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new MercadianAtlas());
        harness.setHand(player1, List.of(new FlamingSword()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
    }

    @Test
    void canEnchantOpponentsCreatureWithoutBoostingOtherCreatures() {
        Permanent ownCreature = addCreature();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new FlamingSword()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Flaming Sword").getAttachedTo()).isEqualTo(opponentCreature.getId());
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void goesToGraveyardWhenTargetLeavesBeforeResolution() {
        Permanent creature = addCreature();
        harness.setHand(player1, List.of(new FlamingSword()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.setGraveyard(player1, List.of(creature.getCard()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flaming Sword");
        harness.assertInGraveyard(player1, "Flaming Sword");
    }

    @Test
    void auraGoesToGraveyardWhenEnchantedCreatureLeaves() {
        Permanent creature = addCreature();
        attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.setGraveyard(player1, List.of(creature.getCard()));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Flaming Sword");
        harness.assertInGraveyard(player1, "Flaming Sword");
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FlamingSword());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
