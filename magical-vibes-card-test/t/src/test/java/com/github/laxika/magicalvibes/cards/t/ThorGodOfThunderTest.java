package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorGodOfThunder.class, Divination.class, Forest.class, GrizzlyBears.class,
        Island.class, LeoninScimitar.class, LoxodonWarhammer.class, Shock.class})
class ThorGodOfThunderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB only offers an Equipment, instant, or sorcery from your graveyard")
    void etbFiltersGraveyardTargets() {
        LeoninScimitar equipment = new LeoninScimitar();
        Shock instant = new Shock();
        Divination sorcery = new Divination();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), equipment, new Island(), instant, sorcery));
        harness.setGraveyard(player2, List.of(new Shock()));
        castThor();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(equipment.getId(), instant.getId(), sorcery.getId());
    }

    @Test
    @DisplayName("ETB exiles the chosen card and lets its controller play it")
    void etbExilesChosenCardAndGrantsPlayPermission() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castThor();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
    }

    @Test
    @DisplayName("Casting a noncreature spell deals damage equal to its mana value")
    void noncreatureSpellDealsManaValueDamage() {
        harness.addToBattlefield(player1, new ThorGodOfThunder());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castFromHand(player1, new Divination(), "{2}{U}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Thor")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorGodOfThunder());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("ETB cannot target a creature card")
    void etbCannotTargetCreatureCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castThor();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Thor's triggered damage uses lifelink granted by Equipment")
    void triggeredDamageUsesGrantedLifelink() {
        Permanent thor = harness.addToBattlefieldAndReturn(player1, new ThorGodOfThunder());
        harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer()).setAttachedTo(thor.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The exiled instant can be cast and triggers Thor")
    void castsExiledInstant() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castThor();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("Casting Equipment triggers Thor for its mana value")
    void equipmentSpellTriggersDamage() {
        harness.addToBattlefield(player1, new ThorGodOfThunder());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The exile permission lasts through your next turn and then expires")
    void permissionExpiresAfterYourNextTurn() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castThor();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(shock.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Thor")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorGodOfThunder());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    private void castThor() {
        harness.castFromHand(player1, new ThorGodOfThunder(), "{3}{R}{R}");
        harness.passBothPriorities();
    }
}
