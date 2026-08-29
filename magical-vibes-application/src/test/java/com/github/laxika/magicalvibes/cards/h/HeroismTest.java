package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BrassclawOrcs;
import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Heroism.class, FarrelitePriest.class, BrassclawOrcs.class, RiverMerfolk.class})
class HeroismTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a white creature prevents unpaid attacking red creature damage")
    void preventsUnpaidAttackingRedCreatureDamage() {
        addHeroismAndWhiteCreature();
        Permanent attacker = addAttacker(player2, new BrassclawOrcs());
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
        addAttacker(player2, new BrassclawOrcs());
        harness.setLife(player1, 20);
        harness.addMana(player2, ManaColor.RED, 3);

        activateHeroism();

        harness.handleMayAbilityChosen(player2, true);
        resolveCombat(player2);

        harness.assertLife(player1, 17);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Accepting without enough mana still prevents attacking red creature damage")
    void insufficientPaymentStillPreventsAttackingRedCreatureDamage() {
        addHeroismAndWhiteCreature();
        Permanent attacker = addAttacker(player2, new BrassclawOrcs());
        harness.setLife(player1, 20);

        activateHeroism();

        harness.handleMayAbilityChosen(player2, true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Heroism resolves without a payment choice when no red creature is attacking")
    void resolvesWithoutAttackingRedCreature() {
        addHeroismAndWhiteCreature();

        activateHeroism();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Heroism cannot be activated without a white creature to sacrifice")
    void cannotActivateWithoutWhiteCreature() {
        harness.addToBattlefield(player1, new Heroism());
        harness.addToBattlefield(player1, new RiverMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only attacking red creatures receive Heroism payment choices")
    void onlyAttackingRedCreaturesAreAffected() {
        addHeroismAndWhiteCreature();
        Permanent redAttacker = addAttacker(player2, new BrassclawOrcs());
        Permanent secondRedAttacker = addAttacker(player2, new BrassclawOrcs());
        addAttacker(player2, new RiverMerfolk());
        Permanent redNotAttacking = harness.addToBattlefieldAndReturn(player2, new BrassclawOrcs());
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
        harness.addToBattlefield(player1, new FarrelitePriest());
    }

    private void activateHeroism() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Farrelite Priest");
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
