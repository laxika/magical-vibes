package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.k.KarnsTemporalSundering;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.ShannaSisaysLegacy;
import com.github.laxika.magicalvibes.cards.s.SparringConstruct;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranTemporalGateway.class, SparringConstruct.class, BalothGorger.class, Opt.class,
        KarnsTemporalSundering.class, ShannaSisaysLegacy.class, TheFlameOfKeld.class})
class ThranTemporalGatewayTest extends BaseCardTest {

    

    @Test
    @DisplayName("Activating ability taps Gateway, spends mana, and goes on stack")
    void activatingAbilityUsesTapAndMana() {
        Permanent gateway = addReadyGateway();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Thran Temporal Gateway");
    }

    @Test
    @DisplayName("Resolving ability prompts may choice first")
    void resolvingPromptsMayChoiceFirst() {
        addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may then resolving prompts card choice with only historic permanent indices")
    void resolvingPromptsOnlyHistoricPermanentChoices() {
        addReadyGateway();
        // SparringConstruct is an artifact creature (historic permanent) — index 0
        // BalothGorger is a regular creature (not historic) — index 1
        // Opt is an instant (not a permanent) — index 2
        harness.setHand(player1, List.of(new SparringConstruct(), new BalothGorger(), new Opt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a historic permanent puts it onto the battlefield")
    void choosingHistoricPermanentPutsItOntoBattlefield() {
        addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Sparring Construct");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining may leaves hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();
        int battlefieldSizeBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    @Test
    @DisplayName("No historic permanent cards in hand skips choice")
    void noHistoricPermanentsInHandSkipsChoice() {
        addReadyGateway();
        // BalothGorger is not historic, Opt is not a permanent
        harness.setHand(player1, List.of(new BalothGorger(), new Opt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("has no historic permanent cards in hand"));
    }

    @Test
    @DisplayName("Non-creature artifact can activate tap ability the turn it enters")
    void canActivateWithoutSummoningSickness() {
        Permanent gateway = harness.addToBattlefieldAndReturn(player1, new ThranTemporalGateway());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent gateway = addReadyGateway();
        gateway.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyGateway();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Artifacts, legendary permanents and Sagas qualify, but legendary sorceries do not")
    void allHistoricPermanentCategoriesAreEligible() {
        addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct(), new ShannaSisaysLegacy(),
                new TheFlameOfKeld(), new KarnsTemporalSundering(), new BalothGorger(), new Opt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("A legendary creature enters without paying its colored mana cost")
    void legendaryCreatureEntersWithoutBeingCast() {
        addReadyGateway();
        ShannaSisaysLegacy shanna = new ShannaSisaysLegacy();
        harness.setHand(player1, List.of(shanna));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(shanna);
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.isSummoningSick()).isTrue();
                });
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("A Saga put onto the battlefield triggers its first chapter")
    void sagaEntersAndResolvesFirstChapter() {
        addReadyGateway();
        harness.setHand(player1, List.of(new TheFlameOfKeld(), new Opt()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "The Flame of Keld");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isInstanceOf(TheFlameOfKeld.class);
                    assertThat(permanent.getCounterCount(CounterType.LORE)).isEqualTo(1);
                });
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Opt);
    }

    @Test
    @DisplayName("The ability still puts a card onto the battlefield after Gateway leaves")
    void abilityResolvesWithoutGatewayOnBattlefield() {
        Permanent gateway = addReadyGateway();
        harness.setHand(player1, List.of(new SparringConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(gateway);
        harness.getGameData().playerGraveyards.get(player1.getId()).add(gateway.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Sparring Construct");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private Permanent addReadyGateway() {
        Permanent gateway = harness.addToBattlefieldAndReturn(player1, new ThranTemporalGateway());
        gateway.setSummoningSick(false);
        return gateway;
    }
}
