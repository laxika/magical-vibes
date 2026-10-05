package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DreadfeastDemon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VexingDevil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaceOfDisruption.class, GrizzlyBears.class, DreadfeastDemon.class, VexingDevil.class})
class MaceOfDisruptionTest extends BaseCardTest {

    @Test
    void equippingGivesToughnessAndProtectionFromDemonsAndDevils() {
        Permanent mace = addCreatureReady(player1, new MaceOfDisruption());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent demon = addCreatureReady(player2, new DreadfeastDemon());
        Permanent devil = addCreatureReady(player2, new VexingDevil());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, creature, demon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, creature, devil)).isTrue();
    }

    @Test
    void attackPerpetuallyBoostsEquippedCreatureWhenAnotherControlledCreatureSharesItsName() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = addCreatureReady(player1, new MaceOfDisruption());
        mace.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        mace.setAttachedTo(null);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void attackPerpetuallyBoostsEquippedCreatureWhenCreatureCardSharesItsNameInGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = addCreatureReady(player1, new MaceOfDisruption());
        mace.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    void attackDoesNotBoostEquippedCreatureWithoutANameMatch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = addCreatureReady(player1, new MaceOfDisruption());
        mace.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void opponentNameMatchesDoNotQualifyForTheBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        Permanent mace = addCreatureReady(player1, new MaceOfDisruption());
        mace.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void detachingMaceBeforeResolutionStillBoostsTheAttackingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = addCreatureReady(player1, new MaceOfDisruption());
        mace.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        mace.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void departedAttackerDoesNotTransferItsBoostToANewlyEquippedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = addCreatureReady(player1, new MaceOfDisruption());
        mace.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        mace.setAttachedTo(replacement.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(2);
    }
}
