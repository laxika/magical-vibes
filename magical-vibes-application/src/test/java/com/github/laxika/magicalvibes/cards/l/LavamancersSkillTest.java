package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavamancersSkill.class, FugitiveWizard.class, GrizzlyBears.class, FountainOfYouth.class})
class LavamancersSkillTest extends BaseCardTest {

    @Test
    void nonWizardGetsOneDamageAbility() {
        addEnchantedCreature(new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void wizardGetsTwoDamageAbility() {
        addEnchantedCreature(new FugitiveWizard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void nonWizardDoesNotGetWizardAbility() {
        addEnchantedCreature(new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedAbilityCanTargetOnlyCreatures() {
        addEnchantedCreature(new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void auraCanEnchantOnlyCreatures() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LavamancersSkill()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addEnchantedCreature(Card card) {
        Permanent creature = addCreatureReady(player1, card);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LavamancersSkill());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
