package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarettiRocketeerEngineer.class, DeathsPresence.class, GrizzlyBears.class,
        MyrBattlesphere.class, Spellbook.class, Terror.class})
class DarettiRocketeerEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Power is the greatest mana value among artifacts you control")
    void powerUsesGreatestControlledArtifactManaValue() {
        Permanent daretti = addReadyDaretti();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new MyrBattlesphere());

        assertThat(gqs.getEffectivePower(gd, daretti)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, daretti)).isEqualTo(5);
    }

    @Test
    @DisplayName("Death triggers use Daretti's effective power before it leaves the battlefield")
    void deathTriggersUseDarettisLastKnownPower() {
        harness.addToBattlefield(player2, new DeathsPresence());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new MyrBattlesphere());
        Permanent daretti = harness.addToBattlefieldAndReturn(player2, new DarettiRocketeerEngineer());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, daretti.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("ETB chooses the artifact card before asking whether to sacrifice")
    void etbTargetsThenOffersSacrifice() {
        Spellbook returnTarget = new Spellbook();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setGraveyard(player1, List.of(returnTarget));
        castDaretti();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(returnTarget.getId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnTarget.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the targeted artifact in the graveyard")
    void decliningSacrificeDoesNothing() {
        Spellbook returnTarget = new Spellbook();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setGraveyard(player1, List.of(returnTarget));
        castDaretti();

        harness.handleMultipleCardsChosen(player1, List.of(returnTarget.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(returnTarget.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
    }

    private Permanent addReadyDaretti() {
        Permanent daretti = harness.addToBattlefieldAndReturn(player1, new DarettiRocketeerEngineer());
        daretti.setSummoningSick(false);
        return daretti;
    }

    private void castDaretti() {
        harness.setHand(player1, List.of(new DarettiRocketeerEngineer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
