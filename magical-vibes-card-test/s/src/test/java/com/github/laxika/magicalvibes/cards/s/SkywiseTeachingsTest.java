package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywiseTeachings.class, Shock.class, GrizzlyBears.class})
class SkywiseTeachingsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell and paying {1}{U} creates a flying Djinn Monk")
    void noncreatureSpellAndPaymentCreateToken() {
        addSkywiseTeachings();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 2
                        && permanent.getCard().getColor() == CardColor.BLUE
                        && permanent.getCard().getSubtypes().containsAll(
                                List.of(CardSubtype.DJINN, CardSubtype.MONK))
                        && permanent.getCard().getKeywords().contains(Keyword.FLYING));
    }

    @Test
    @DisplayName("Declining the payment creates no token")
    void decliningPaymentCreatesNoToken() {
        addSkywiseTeachings();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.DJINN));
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Skywise Teachings")
    void creatureSpellDoesNotTrigger() {
        addSkywiseTeachings();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Skywise Teachings")
    void opponentSpellDoesNotTrigger() {
        addSkywiseTeachings();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Skywise Teachings does not trigger from its own casting")
    void ownCastingDoesNotTrigger() {
        harness.setHand(player1, List.of(new SkywiseTeachings()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skywise Teachings");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("An existing Teachings triggers from casting another noncreature permanent")
    void anotherTeachingsTriggersExistingTeachings() {
        addSkywiseTeachings();
        harness.setHand(player1, List.of(new SkywiseTeachings()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof SkywiseTeachings).hasSize(2);
    }

    @Test
    @DisplayName("Each noncreature spell can create a token with a separate payment")
    void successiveSpellsCanEachCreateToken() {
        addSkywiseTeachings();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Generic mana cannot replace the blue mana in the optional payment")
    void paymentWithoutBlueManaCreatesNoToken() {
        addSkywiseTeachings();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.assertLife(player2, 18);
    }

    private void addSkywiseTeachings() {
        harness.addToBattlefield(player1, new SkywiseTeachings());
    }
}
