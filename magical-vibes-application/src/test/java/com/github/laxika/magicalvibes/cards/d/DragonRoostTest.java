package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DragonRoost.class)
class DragonRoostTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dragon Roost puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new DragonRoost(), "{4}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Dragon Roost");
    }

    @Test
    @DisplayName("Resolving puts Dragon Roost onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new DragonRoost(), "{4}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dragon Roost");
    }

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Dragon Roost");
    }

    @Test
    @DisplayName("Resolving ability creates a 5/5 Dragon token")
    void resolvingAbilityCreatesToken() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Dragon");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
    }

    @Test
    @DisplayName("Dragon token has flying")
    void dragonTokenHasFlying() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dragon");
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Dragon token is a creature")
    void dragonTokenIsCreature() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dragon");
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Dragon token enters with summoning sickness")
    void tokenEntersWithSummoningSickness() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dragon");
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 9);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate ability multiple times with enough mana")
    void canActivateMultipleTimes() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 14);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = countPermanents(player1, "Dragon");
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Can pay five generic mana and two red mana for the ability")
    void canPayGenericAndRedPartsSeparately() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot pay the ability with only one red mana")
    void cannotActivateWithOnlyOneRedMana() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Dragon Roost remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragon Roost");
    }

    @Test
    @DisplayName("Creating Dragon token is logged")
    void tokenCreationIsLogged() {
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Dragon") && log.contains("token"));
    }
}

