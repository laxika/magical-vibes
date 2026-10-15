package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DiregrafCaptain;
import com.github.laxika.magicalvibes.cards.f.FlayerOfTheHatebound;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorinLordOfInnistrad.class, GrizzlyBears.class, LilianaOfTheVeil.class,
        DiregrafCaptain.class, FlayerOfTheHatebound.class})
class SorinLordOfInnistradTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new SorinLordOfInnistrad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent sorin = findPermanent(player1, "Sorin, Lord of Innistrad");
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 creates a 1/1 black Vampire token with lifelink")
    void plusOneCreatesVampireToken() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Vampire");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 creates an emblem with +1/+0 for creatures you control")
    void minusTwoCreatesEmblem() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.emblems).hasSize(1);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("-2 logs the emblem against the activating player's name")
    void minusTwoLogsEmblemAgainstController() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .contains(player1.getUsername() + " gets an emblem with \"Creatures you control get +1/+0.\".");
    }

    @Test
    @DisplayName("Emblem gives creatures you control +1/+0")
    void emblemBoostsControlledCreatures() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 3);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("-6 destroys up to three creatures and returns them under your control")
    void minusSixDestroysAndReturnsCreatures() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        List<UUID> targetIds = findPermanents(player2, "Grizzly Bears").stream()
                .map(Permanent::getId)
                .toList();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, targetIds);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 can target fewer than three permanents")
    void minusSixCanTargetFewerThanThree() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(bearsId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-6 can be activated with zero targets")
    void minusSixCanActivateWithZeroTargets() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-6 returns destroyed planeswalkers under your control")
    void minusSixReturnsDestroyedPlaneswalker() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new LilianaOfTheVeil());
        Permanent liliana = findPermanent(player2, "Liliana of the Veil");
        liliana.setCounterCount(CounterType.LOYALTY, 3);

        UUID lilianaId = liliana.getId();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(lilianaId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana of the Veil");
        Permanent returned = findPermanent(player1, "Liliana of the Veil");
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 does not return indestructible permanents")
    void minusSixDoesNotReturnIndestructiblePermanent() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsId = bears.getId();
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(bearsId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target Sorin himself with -6")
    void cannotTargetSelfWithMinusSix() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(sorin.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another planeswalker");
    }

    @Test
    @DisplayName("Cannot activate -6 with insufficient loyalty")
    void cannotActivateMinusSixWithInsufficientLoyalty() {
        addReadySorin(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Creatures destroyed together see each other's deaths")
    void minusSixDestroysTargetsSimultaneously() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DiregrafCaptain());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DiregrafCaptain());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player2, player1.getId());
            resolveAllTriggers();
        }

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(countPermanents(player1, "Diregraf Captain")).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Diregraf Captain");
    }

    @Test
    @DisplayName("Flayer returned together with another creature sees both entries")
    void minusSixReturnsTargetsSimultaneously() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new DiregrafCaptain());
        Permanent flayer = harness.addToBattlefieldAndReturn(player1, new FlayerOfTheHatebound());
        flayer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(captain.getId(), flayer.getId()));
        harness.passBothPriorities();

        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();
        }

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
        harness.assertOnBattlefield(player1, "Diregraf Captain");
        harness.assertOnBattlefield(player1, "Flayer of the Hatebound");
    }

    @Test
    @DisplayName("Stolen Flayer does not trigger for entering from an opponent's graveyard")
    void minusSixPreservesActualGraveyardOwner() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);
        Permanent flayer = harness.addToBattlefieldAndReturn(player2, new FlayerOfTheHatebound());
        flayer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(flayer.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flayer of the Hatebound");
        harness.assertNotOnBattlefield(player2, "Flayer of the Hatebound");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ultimate still resolves when paying six loyalty puts Sorin in the graveyard")
    void minusSixResolvesAfterSourceDies() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DiregrafCaptain());

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sorin, Lord of Innistrad");
        harness.assertOnBattlefield(player1, "Diregraf Captain");
        harness.assertNotOnBattlefield(player2, "Diregraf Captain");
    }

    @Test
    @DisplayName("Emblem continues to boost creatures entering after Sorin leaves")
    void emblemPersistsWithoutSorin() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sorin, Lord of Innistrad");

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DiregrafCaptain());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent addReadySorin(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SorinLordOfInnistrad());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
