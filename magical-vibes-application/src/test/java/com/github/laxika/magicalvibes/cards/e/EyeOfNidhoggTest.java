package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeOfNidhogg.class, ElvishLyrist.class, FountainOfYouth.class, GrizzlyBears.class})
class EyeOfNidhoggTest extends BaseCardTest {

    @Test
    void enchantedCreatureBecomesBlackDragonWithGrantedAbilities() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    void effectsStopWhenAuraIsRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachAura(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.BEAR);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void returnsToOwnersHandWhenPutIntoGraveyardFromBattlefield() {
        Permanent aura = attachAura(player1, addCreatureReady(player1, new GrizzlyBears()));
        addCreatureReady(player2, new ElvishLyrist());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 0, null, aura.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Eye of Nidhogg");
        harness.assertNotInGraveyard(player1, "Eye of Nidhogg");
        harness.assertNotOnBattlefield(player1, "Eye of Nidhogg");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new EyeOfNidhogg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void returnsToOwnerWhenControlledByAnotherPlayer() {
        Permanent aura = attachAura(player1, addCreatureReady(player1, new GrizzlyBears()));
        aura.getCard().setOwnerId(player2.getId());
        addCreatureReady(player2, new ElvishLyrist());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 0, null, aura.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Eye of Nidhogg");
        harness.assertNotInHand(player1, "Eye of Nidhogg");
        harness.assertNotInGraveyard(player2, "Eye of Nidhogg");
    }

    @Test
    void canCastAndResolveOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EyeOfNidhogg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Eye of Nidhogg").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }
    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new EyeOfNidhogg());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
