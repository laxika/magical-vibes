package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunhomeGuildmage.class})
class SunhomeGuildmageTest extends BaseCardTest {

    private void addMana(Player player, int generic) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, generic);
    }

    @Test
    @DisplayName("First ability gives +1/+0 to creatures you control only")
    void boostsOwnCreaturesOnly() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());
        Permanent ownGuildmage = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());
        Permanent opposingGuildmage = harness.addToBattlefieldAndReturn(player2, new SunhomeGuildmage());

        harness.forceActivePlayer(player1);
        addMana(player1, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(ownGuildmage.getEffectivePower()).isEqualTo(3);
        assertThat(ownGuildmage.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingGuildmage.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());

        harness.forceActivePlayer(player1);
        addMana(player1, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();
        assertThat(source.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(0);
        assertThat(source.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability creates a 1/1 Soldier token with haste")
    void createsHastySoldierToken() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());

        harness.forceActivePlayer(player1);
        addMana(player1, 2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Soldier");

        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Both abilities work while tapped and summoning sick, and boosts stack")
    void abilitiesWorkWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());
        source.tap();
        source.setSummoningSick(true);
        harness.forceActivePlayer(player1);

        addMana(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).get(1);

        for (int i = 0; i < 2; i++) {
            addMana(player1, 1);
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(source.getEffectivePower()).isEqualTo(4);
        assertThat(source.getEffectiveToughness()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A token created after the boost resolves does not receive that boost")
    void laterTokenDoesNotReceiveBoost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());
        harness.forceActivePlayer(player1);
        addMana(player1, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        addMana(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A token created in response to the boost receives it on resolution")
    void tokenCreatedInResponseReceivesBoost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());
        harness.forceActivePlayer(player1);
        addMana(player1, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        addMana(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated token creation makes separate untapped red and white tokens")
    void createsSeparateRedAndWhiteTokens() {
        harness.addToBattlefield(player1, new SunhomeGuildmage());
        harness.forceActivePlayer(player1);

        for (int i = 0; i < 2; i++) {
            addMana(player1, 2);
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).subList(1, 3)).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("The Soldier can attack on the turn it is created")
    void soldierCanAttackImmediately() {
        harness.addToBattlefield(player1, new SunhomeGuildmage());
        harness.forceActivePlayer(player1);
        addMana(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 20);
        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 19);
    }
}
