package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedMagesRapier.class, DarkRitual.class, GrizzlyBears.class})
class RedMagesRapierTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new RedMagesRapier()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rapier = findPermanent(player1, "Red Mage's Rapier");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(rapier.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.WIZARD);
    }

    @Test
    void equippedCreatureGetsBoostFromNoncreatureSpell() {
        Permanent rapier = addRapierReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        rapier.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerFromCreatureSpell() {
        Permanent rapier = addRapierReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        rapier.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void equippingMovesWizardSubtypeToNewCreature() {
        Permanent rapier = addRapierReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        rapier.setAttachedTo(first.getId());

        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).contains(CardSubtype.WIZARD);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).doesNotContain(CardSubtype.WIZARD);

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(rapier.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.WIZARD);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.WIZARD);
    }

    private Permanent addRapierReady(Player player) {
        Permanent permanent = new Permanent(new RedMagesRapier());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
