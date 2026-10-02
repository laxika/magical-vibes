package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilitiasPride.class, GoldmeadowDodger.class})
class MilitiasPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken attacker: pay {W} creates a tapped, attacking 1/1 Kithkin Soldier")
    void payCreatesTappedAttackingToken() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player1, new GoldmeadowDodger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        // The token entered tapped and attacking. handleMayAbilityChosen auto-passes past
        // END_OF_COMBAT, which clears isAttacking; assert attackedThisTurn + the game log instead.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()
                        && p.getCard().getSubtypes().contains(CardSubtype.KITHKIN)
                        && p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(p.isTapped()).isTrue();
                    assertThat(p.isAttackedThisTurn()).isTrue();
                });

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Kithkin Soldier") && log.contains("tapped and attacking"));
    }

    @Test
    @DisplayName("Declining the may-pay creates no token")
    void declineCreatesNoToken() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player1, new GoldmeadowDodger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("A token creature attacking does not trigger the ability")
    void tokenAttackerDoesNotTrigger() {
        addCreatureReady(player1, new MilitiasPride());
        Permanent tokenAttacker = addCreatureReady(player1, new GoldmeadowDodger());
        TestCards.mutableCard(tokenAttacker).setToken(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting with insufficient mana creates no token")
    void insufficientManaCreatesNoToken() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player1, new GoldmeadowDodger());
        // No mana added.

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Each nontoken attacker triggers separately")
    void eachNontokenAttackerTriggersSeparately() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player1, new GoldmeadowDodger());
        addCreatureReady(player1, new GoldmeadowDodger());
        harness.addMana(player1, ManaColor.WHITE, 2);

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("An opponent's attacker does not trigger the ability")
    void opponentAttackerDoesNotTrigger() {
        addCreatureReady(player1, new MilitiasPride());
        addCreatureReady(player2, new GoldmeadowDodger());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }
}
