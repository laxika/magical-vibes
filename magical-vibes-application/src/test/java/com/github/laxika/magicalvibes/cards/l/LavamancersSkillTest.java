package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.r.RiptideBiologist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavamancersSkill.class, RiptideBiologist.class, GlorySeeker.class,
        ElvishWarrior.class, Forest.class})
class LavamancersSkillTest extends BaseCardTest {

    @Test
    void resolvingAuraAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        harness.setHand(player1, List.of(new LavamancersSkill()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lavamancer's Skill").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    void nonWizardGetsOneDamageAbility() {
        addEnchantedCreature(new GlorySeeker());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void wizardGetsTwoDamageAbility() {
        addEnchantedCreature(new RiptideBiologist());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void wizardTwoDamageAbilityDealsExactlyTwoDamage() {
        addEnchantedCreature(new RiptideBiologist());
        Permanent target = addCreatureReady(player2, new ElvishWarrior());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void wizardAlsoGetsOneDamageAbility() {
        Permanent wizard = addEnchantedCreature(new RiptideBiologist());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(wizard.isTapped()).isTrue();
    }

    @Test
    void nonWizardDoesNotGetWizardAbility() {
        addEnchantedCreature(new GlorySeeker());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedAbilityCanTargetOnlyCreatures() {
        addEnchantedCreature(new GlorySeeker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void auraCanEnchantOnlyCreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LavamancersSkill()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickEnchantedCreatureCannotActivateGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LavamancersSkill());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void opponentControlsAbilityGrantedByYourAura() {
        Permanent creature = addCreatureReady(player2, new GlorySeeker());
        Permanent target = addCreatureReady(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new LavamancersSkill()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Lavamancer's Skill").isTapped()).isFalse();
    }

    @Test
    void tappingForOneAbilityPreventsActivatingTheOther() {
        Permanent wizard = addEnchantedCreature(new RiptideBiologist());
        Permanent target = addCreatureReady(player2, new ElvishWarrior());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(wizard.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void grantedAbilityCanTargetItsOwnSource() {
        Permanent creature = addEnchantedCreature(new GlorySeeker());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void removingAuraDoesNotStopAlreadyActivatedAbility() {
        Permanent wizard = addEnchantedCreature(new RiptideBiologist());
        Permanent target = addCreatureReady(player2, new ElvishWarrior());
        Permanent aura = findPermanent(player1, "Lavamancer's Skill");

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        wizard.setTapped(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addEnchantedCreature(Card card) {
        Permanent creature = addCreatureReady(player1, card);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LavamancersSkill());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
