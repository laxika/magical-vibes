package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeroismTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a white creature prevents unpaid attacking red creature damage")
    void preventsUnpaidAttackingRedCreatureDamage() {
        addHeroismAndWhiteCreature();
        Permanent attacker = addAttacker(player2, new FireElemental());
        harness.setLife(player1, 20);

        activateHeroism();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, false);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("An attacking red creature's controller may pay to deal combat damage")
    void paymentAllowsAttackingRedCreatureDamage() {
        addHeroismAndWhiteCreature();
        addAttacker(player2, new FireElemental());
        harness.setLife(player1, 20);
        harness.addMana(player2, ManaColor.RED, 3);

        activateHeroism();

        harness.handleMayAbilityChosen(player2, true);
        resolveCombat(player2);

        harness.assertLife(player1, 15);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Only attacking red creatures receive Heroism payment choices")
    void onlyAttackingRedCreaturesAreAffected() {
        addHeroismAndWhiteCreature();
        Permanent redAttacker = addAttacker(player2, new FireElemental());
        Permanent secondRedAttacker = addAttacker(player2, new FireElemental());
        addAttacker(player2, new GrizzlyBears());
        Permanent redNotAttacking = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        harness.setLife(player1, 20);

        activateHeroism();

        assertThat(gd.pendingMayAbilities).hasSize(2);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage)
                .contains(redAttacker.getId(), secondRedAttacker.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(redNotAttacking.getId());
    }

    private void addHeroismAndWhiteCreature() {
        harness.addToBattlefield(player1, new Heroism());
        harness.addToBattlefield(player1, new SavannahLions());
    }

    private void activateHeroism() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
