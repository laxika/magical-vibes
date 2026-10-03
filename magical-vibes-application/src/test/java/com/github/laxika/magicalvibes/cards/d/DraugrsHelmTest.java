package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DraugrsHelm.class, DeathknellBerserker.class})
class DraugrsHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the ETB cost creates and equips a Zombie Berserker")
    void payingEtbCostCreatesAndEquipsZombieBerserker() {
        Permanent helm = castHelmWithMana(3, 2);

        harness.handleMayAbilityChosen(player1, true);

        Permanent zombieBerserker = findPermanents(player1, "Zombie").getFirst();
        assertThat(helm.getAttachedTo()).isEqualTo(zombieBerserker.getId());
        assertThat(gqs.getEffectivePower(gd, zombieBerserker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombieBerserker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zombieBerserker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB cost creates no Zombie Berserker")
    void decliningEtbCostCreatesNoZombieBerserker() {
        Permanent helm = castHelmWithMana(3, 2);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {4} gives the equipped creature +2/+2 and menace")
    void equipAttachesHelmAndGrantsBonus() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new DraugrsHelm());
        Permanent creature = addCreatureReady(player1, new DeathknellBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Moving the Helm removes its bonuses from the previous creature")
    void reequippingMovesBothBonuses() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new DraugrsHelm());
        Permanent first = addCreatureReady(player1, new DeathknellBerserker());
        Permanent second = addCreatureReady(player1, new DeathknellBerserker());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The token is still created if the Helm has left the battlefield")
    void tokenIsCreatedWithoutHelm() {
        Permanent helm = castHelmWithMana(3, 2);
        gd.playerBattlefields.get(player1.getId()).remove(helm);
        gd.playerGraveyards.get(player1.getId()).add(helm.getCard());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent token = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isFalse();
        assertThat(findPermanents(player1, "Draugr's Helm")).isEmpty();
    }

    @Test
    @DisplayName("Paying one Helm's trigger does not attach another Helm")
    void onlyTriggeringHelmAttaches() {
        Permanent otherHelm = harness.addToBattlefieldAndReturn(player1, new DraugrsHelm());
        Permanent helm = castHelmWithMana(3, 2);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(helm.getAttachedTo()).isEqualTo(findPermanent(player1, "Zombie").getId());
        assertThat(otherHelm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Accepting without enough mana does not create a token")
    void insufficientManaCreatesNoToken() {
        Permanent helm = castHelmWithMana(1, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Creating the token consumes the additional mana payment")
    void creatingTokenPaysAdditionalCost() {
        castHelmWithMana(3, 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent castHelmWithMana(int colorless, int black) {
        harness.setHand(player1, List.of(new DraugrsHelm()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.BLACK, black);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanents(player1, "Draugr's Helm").getLast();
    }
}
