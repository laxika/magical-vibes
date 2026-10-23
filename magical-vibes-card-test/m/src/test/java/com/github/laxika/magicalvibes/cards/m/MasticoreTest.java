package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Masticore.class, BraidwoodCup.class})
class MasticoreTest extends BaseCardTest {

    @Test
    void upkeepCanBePaidByDiscardingAnyCard() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of(new BraidwoodCup()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Braidwood Cup");
    }

    @Test
    void upkeepSacrificesMasticoreWhenDiscardIsDeclined() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of(new BraidwoodCup()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Masticore");
    }

    @Test
    void upkeepSacrificesMasticoreWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Masticore");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void damageAbilityDealsOneDamageToTargetCreature() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addToBattlefield(player2, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID masticoreId = harness.getPermanentId(player2, "Masticore");

        harness.activateAbility(player1, 0, 0, null, masticoreId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Masticore");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void damageAbilityRejectsPlayerTarget() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    @Test
    void damageAbilityRejectsNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addToBattlefield(player2, new BraidwoodCup());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID cupId = harness.getPermanentId(player2, "Braidwood Cup");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, cupId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void abilitiesRequireTwoColorlessMana() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addToBattlefield(player2, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID masticoreId = harness.getPermanentId(player2, "Masticore");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, masticoreId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void regenerationAbilityGrantsARegenerationShield() {
        harness.addToBattlefield(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesMasticoreFromLethalDamage() {
        Permanent masticore = addCreatureReady(player1, new Masticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, 0, null, masticore.getId());
        }
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Masticore");
        Permanent survivor = findPermanent(player1, "Masticore");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isZero();
        assertThat(survivor.getMarkedDamage()).isZero();
    }

    @Test
    void upkeepTriggerOnlyFiresDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player2);

        harness.assertOnBattlefield(player1, "Masticore");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappedSummoningSickMasticoreCanActivateBothAbilitiesWithColoredMana() {
        harness.addToBattlefield(player1, new Masticore());
        Permanent masticore = findPermanent(player1, "Masticore");
        masticore.tap();
        masticore.setSummoningSick(true);
        harness.addToBattlefield(player2, new Masticore());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Masticore"));
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Masticore").getMarkedDamage()).isEqualTo(1);
        assertThat(masticore.getRegenerationShield()).isEqualTo(1);
        assertThat(masticore.isTapped()).isTrue();
    }

    @Test
    void regenerationCannotPreventUpkeepSacrifice() {
        harness.addToBattlefield(player1, new Masticore());
        harness.setHand(player1, List.of());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Masticore").getRegenerationShield()).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Masticore");
        harness.assertInGraveyard(player1, "Masticore");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Declining the discard asks for Masticore's death replacement before it is sacrificed")
    void decliningAsksForTheDeathReplacementBeforeMasticoreIsSacrificed() {
        Permanent masticore = harness.addToBattlefieldAndReturn(player1, new Masticore());
        enchantWithMagemarkAgainstKalitas(masticore);
        harness.setHand(player1, List.of(new BraidwoodCup()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertDeathReplacementAskedWhileMasticoreStays(masticore);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertMasticoreReturnedToHandWithoutZombie(masticore);
        harness.assertInHand(player1, "Braidwood Cup");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Accepting with an empty hand asks for Masticore's death replacement before it is sacrificed")
    void acceptingWithNothingLeftAsksForTheDeathReplacementBeforeMasticoreIsSacrificed() {
        Permanent masticore = harness.addToBattlefieldAndReturn(player1, new Masticore());
        enchantWithMagemarkAgainstKalitas(masticore);
        harness.setHand(player1, List.of(new BraidwoodCup()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertDeathReplacementAskedWhileMasticoreStays(masticore);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertMasticoreReturnedToHandWithoutZombie(masticore);
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    private void assertDeathReplacementAskedWhileMasticoreStays(Permanent creature) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.rerunsEffect()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();
    }

    private void assertMasticoreReturnedToHandWithoutZombie(Permanent creature) {
        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void enchantWithMagemarkAgainstKalitas(Permanent creature) {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(creature.getId());
    }
}
